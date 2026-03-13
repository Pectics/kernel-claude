package me.pectics.kernelclaude.event;

import lombok.extern.slf4j.Slf4j;
import me.pectics.kernelclaude.event.model.KernelEvent;
import me.pectics.kernelclaude.event.model.SignedEvent;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 事件总线的内存实现
 * <p>
 * 特点：
 * 1. 使用优先级队列处理事件
 * 2. 支持异步发布
 * 3. 按优先级顺序分发给监听器
 * <p>
 * 注意：这是简化版本，暂未实现持久化。
 * 后续会添加 EventStore 实现双写策略。
 * <p>
 * 注意：此类不包含 @Component 注解，
 * 应在 app 模块中通过 @Configuration 配置注册为 Bean。
 */
@Slf4j
public class InMemoryEventBus implements EventBus {

    /**
     * 优先级队列 - 按事件优先级排序
     * 使用 ConcurrentLinkedQueue 保证线程安全
     */
    private final PriorityBlockingQueue<SignedEvent> eventQueue;

    /**
     * 注册的监听器列表
     */
    private final List<EventListener> listeners;

    /**
     * 事件处理线程池
     */
    private final ExecutorService executor;

    /**
     * 运行状态标志
     */
    private final AtomicBoolean running;

    /**
     * 事件处理线程
     */
    private Thread dispatchThread;

    public InMemoryEventBus() {
        // 按优先级排序：优先级数值越小越先处理
        this.eventQueue = new PriorityBlockingQueue<>(
                100,
                Comparator.comparingInt(e -> e.event().priority().getLevel())
        );
        this.listeners = new CopyOnWriteArrayList<>();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.running = new AtomicBoolean(false);
    }

    @Override
    public void publish(SignedEvent event) {
        if (event == null) {
            log.warn("Attempted to publish null event");
            return;
        }

        log.debug("Publishing event: id={}, type={}, priority={}",
                event.getEventId(),
                event.event().type(),
                event.event().priority());

        eventQueue.offer(event);

        // TODO: 双写 - 同时写入 EventStore
    }

    @Override
    public void publish(KernelEvent event) {
        // 系统内部事件使用空权限签名
        // 实际使用时应该通过 PermissionManager 签名
        SignedEvent signedEvent = new SignedEvent(
                event,
                java.util.Set.of(),
                "",
                System.currentTimeMillis()
        );
        publish(signedEvent);
    }

    @Override
    public CompletableFuture<Void> publishAsync(SignedEvent event) {
        return CompletableFuture.runAsync(() -> publish(event), executor);
    }

    @Override
    public void registerListener(EventListener listener) {
        if (listener == null) {
            log.warn("Attempted to register null listener");
            return;
        }

        listeners.add(listener);
        listeners.sort(Comparator.comparingInt(EventListener::getOrder));

        log.info("Registered event listener: {} (order={})",
                listener.getName(), listener.getOrder());
    }

    @Override
    public void unregisterListener(EventListener listener) {
        if (listeners.remove(listener)) {
            log.info("Unregistered event listener: {}", listener.getName());
        }
    }

    @Override
    public int getQueueSize() {
        return eventQueue.size();
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            dispatchThread = Thread.ofPlatform()
                    .name("event-dispatcher")
                    .daemon(false)
                    .start(this::dispatchLoop);
            log.info("EventBus started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (dispatchThread != null) {
                dispatchThread.interrupt();
                try {
                    dispatchThread.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            executor.shutdown();
            log.info("EventBus stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 事件分发循环
     * <p>
     * 持续从队列中取出事件，分发给能够处理的监听器
     */
    private void dispatchLoop() {
        while (running.get()) {
            try {
                // 阻塞等待事件
                SignedEvent event = eventQueue.poll(100, TimeUnit.MILLISECONDS);

                if (event != null) {
                    dispatchTo(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Event dispatcher interrupted");
                break;
            } catch (Exception e) {
                log.error("Error in event dispatch loop", e);
            }
        }
    }

    /**
     * 将事件分发给所有能够处理的监听器
     */
    private void dispatchTo(SignedEvent event) {
        log.debug("Dispatching event {} to {} listeners",
                event.getEventId(), listeners.size());

        for (EventListener listener : listeners) {
            try {
                if (listener.canHandle(event)) {
                    // 使用虚拟线程异步执行，避免阻塞
                    executor.execute(() -> {
                        try {
                            listener.onEvent(event);
                        } catch (Exception e) {
                            log.error("Listener {} failed to handle event {}: {}",
                                    listener.getName(), event.getEventId(), e.getMessage(), e);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Error checking if listener {} can handle event: {}",
                        listener.getName(), e.getMessage());
            }
        }
    }
}
