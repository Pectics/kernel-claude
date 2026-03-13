package me.pectics.kernelclaude.event;

/**
 * 事件总线接口
 * <p>
 * 系统的核心组件，负责：<br>
 * 1. 接收所有事件<br>
 * 2. 按优先级排序<br>
 * 3. 分发给注册的监听器<br>
 * 4. 持久化事件（双写策略）
 * <p>
 * 双写策略说明：<br>
 * - 内存队列：高性能消费<br>
 * - 持久化存储：程序重启后可恢复
 */
public interface EventBus extends EventPublisher {

    /**
     * 注册事件监听器
     *
     * @param listener 监听器实例
     */
    void registerListener(EventListener listener);

    /**
     * 注销事件监听器
     *
     * @param listener 监听器实例
     */
    void unregisterListener(EventListener listener);

    /**
     * 启动事件总线
     */
    void start();

    /**
     * 停止事件总线
     * <p>
     * 会等待当前事件处理完成
     */
    void stop();

    /**
     * 获取待处理事件数量
     *
     * @return 队列大小
     */
    int getQueueSize();

    /**
     * 获取事件总线状态
     *
     * @return 是否运行中
     */
    boolean isRunning();
}
