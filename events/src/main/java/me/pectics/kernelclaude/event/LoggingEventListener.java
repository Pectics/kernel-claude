package me.pectics.kernelclaude.event;

import lombok.extern.slf4j.Slf4j;
import me.pectics.kernelclaude.event.model.EventType;
import me.pectics.kernelclaude.event.model.SignedEvent;

/**
 * 日志事件监听器
 * <p>
 * 一个简单的事件监听器实现，用于：
 * 1. 记录所有接收到的事件
 * 2. 作为 EventBus 的测试示例
 * <p>
 * 注意：此类不包含 @Component 注解，
 * 应在 app 模块中通过 @Configuration 配置注册为 Bean。
 */
@Slf4j
public class LoggingEventListener implements EventListener {

    @Override
    public void onEvent(SignedEvent event) {
        log.info("""
                [Event Received]
                  ID: {}
                  Type: {}
                  Platform: {}
                  Session: {}
                  User: {}
                  Content: {}
                  """.formatted(
                        event.getEventId(),
                        event.event().type(),
                        event.event().platform(),
                        event.getSessionId(),
                        event.event().userId(),
                        event.event().getMessageContent()
                ));
    }

    @Override
    public boolean canHandle(SignedEvent event) {
        // 只处理消息类型的事件
        return event.event().type() == EventType.MESSAGE;
    }

    @Override
    public String getName() {
        return "LoggingEventListener";
    }

    @Override
    public int getOrder() {
        // 低优先级，让其他监听器先执行
        return 999;
    }
}
