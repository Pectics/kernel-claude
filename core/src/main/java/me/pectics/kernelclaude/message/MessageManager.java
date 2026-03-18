package me.pectics.kernelclaude.message;

import lombok.extern.slf4j.Slf4j;
import me.pectics.kernelclaude.event.EventBus;
import me.pectics.kernelclaude.event.message.MessageReceivedEvent;
import me.pectics.kernelclaude.event.message.MessageRouteFailedEvent;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MessageManager {

    private final EventBus eventBus;
    private final PriorityBlockingQueue<Message> queue;
    private volatile MessageRouter router;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread;

    public MessageManager(EventBus eventBus) {
        this.eventBus = eventBus;
        this.queue = new PriorityBlockingQueue<>();
    }

    public void setRouter(MessageRouter router) {
        this.router = router;
    }

    public void submit(Message message) {
        queue.add(message);
        eventBus.dispatch(new MessageReceivedEvent(message));
    }

    public void start() {
        if (!running.compareAndSet(false, true)) return;

        consumerThread = Thread.ofPlatform()
                .daemon(true)
                .name("message-consumer")
                .start(this::consumeLoop);
    }

    public void stop() {
        running.set(false);
        if (consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getQueueSize() {
        return queue.size();
    }

    private void consumeLoop() {
        while (running.get()) {
            Message message = null;
            try {
                message = queue.take();
                MessageRouter currentRouter = router;
                if (currentRouter == null) {
                    log.warn("No router set, dropping message: {}", message.id());
                    continue;
                }
                currentRouter.route(message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Failed to route message: {}", message != null ? message.id() : "unknown", e);
                if (message != null) {
                    eventBus.dispatch(new MessageRouteFailedEvent(message, e));
                }
            }
        }
    }
}
