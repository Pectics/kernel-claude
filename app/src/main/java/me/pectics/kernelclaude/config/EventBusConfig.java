package me.pectics.kernelclaude.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import me.pectics.kernelclaude.event.EventBus;
import me.pectics.kernelclaude.event.EventListener;
import me.pectics.kernelclaude.event.InMemoryEventBus;
import me.pectics.kernelclaude.event.LoggingEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 事件总线配置
 * <p>
 * 负责：
 * 1. 创建 EventBus 和 EventListener Bean
 * 2. 在应用启动时初始化 EventBus
 * 3. 注册所有 EventListener
 * 4. 在应用关闭时停止 EventBus
 */
@Slf4j
@Configuration
public class EventBusConfig {

    private EventBus eventBus;
    private List<EventListener> listeners;

    /**
     * 创建 EventBus Bean
     */
    @Bean
    public EventBus eventBus() {
        return new InMemoryEventBus();
    }

    /**
     * 创建 LoggingEventListener Bean
     * <p>
     * 注意：后续可以添加更多监听器
     */
    @Bean
    public LoggingEventListener loggingEventListener() {
        return new LoggingEventListener();
    }

    /**
     * 初始化事件总线
     * <p>
     * 在所有 Bean 创建后调用
     */
    @PostConstruct
    public void init() {
        // 通过 Spring 获取所有 EventListener Bean
        // 这里暂时手动注入
        this.listeners = List.of(loggingEventListener());
        this.eventBus = eventBus();

        log.info("Initializing EventBus with {} listeners", listeners.size());

        for (EventListener listener : listeners) {
            eventBus.registerListener(listener);
        }

        eventBus.start();

        log.info("EventBus initialized and started");
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down EventBus...");
        if (eventBus != null) {
            eventBus.stop();
        }
        log.info("EventBus shutdown complete");
    }
}
