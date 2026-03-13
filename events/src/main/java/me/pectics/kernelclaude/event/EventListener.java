package me.pectics.kernelclaude.event;

import me.pectics.kernelclaude.event.model.SignedEvent;

/**
 * 事件监听器接口
 * <p>
 * 所有需要接收事件的组件都应实现此接口，
 * 并通过 {@link EventBus} 注册。
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @Component
 * public class MyEventListener implements EventListener {
 *
 *     @Override
 *     public void onEvent(SignedEvent event) {
 *         // 处理事件
 *     }
 *
 *     @Override
 *     public boolean canHandle(SignedEvent event) {
 *         // 只处理消息事件
 *         return event.event().type() == EventType.MESSAGE;
 *     }
 * }
 * }
 * </pre>
 */
public interface EventListener {

    /**
     * 处理事件
     *
     * @param event 签名事件
     */
    void onEvent(SignedEvent event);

    /**
     * 判断是否可以处理该事件
     * <p>
     * 默认实现返回 true，表示处理所有事件。
     * 子类可以覆盖此方法实现过滤逻辑。
     *
     * @param event 签名事件
     * @return 是否可以处理
     */
    default boolean canHandle(SignedEvent event) {
        return true;
    }

    /**
     * 获取监听器名称
     * <p>
     * 用于日志和调试
     *
     * @return 监听器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取监听器优先级
     * <p>
     * 数值越小优先级越高，默认为 100
     *
     * @return 优先级
     */
    default int getOrder() {
        return 100;
    }
}
