package me.pectics.kernelclaude.event;

import me.pectics.kernelclaude.event.model.KernelEvent;
import me.pectics.kernelclaude.event.model.SignedEvent;

import java.util.concurrent.CompletableFuture;

/**
 * 事件发布者接口
 * <p>
 * 提供事件发布的能力，任何组件都可以通过此接口发布事件。
 * <p>
 * 实现类应该：
 * <br>
 * 1. 将事件写入内存队列（快速消费）
 * <br>
 * 2. 将事件持久化到存储（可靠性）
 */
public interface EventPublisher {

    /**
     * 发布一个已签名的事件
     *
     * @param event 签名事件
     */
    void publish(SignedEvent event);

    /**
     * 发布一个原始事件（会自动签名）
     * <p>
     * 注意：此方法会使用默认权限进行签名，
     * 仅适用于系统内部事件。
     *
     * @param event 原始事件
     */
    void publish(KernelEvent event);

    /**
     * 异步发布事件
     *
     * @param event 签名事件
     * @return 发布完成的 Future
     */
    CompletableFuture<Void> publishAsync(SignedEvent event);
}
