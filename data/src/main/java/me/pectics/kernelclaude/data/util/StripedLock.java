package me.pectics.kernelclaude.data.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * StripedLock 是一种基于分段锁的工具类，用于在多线程环境下对不同的 key 进行锁保护。
 * <p>
 * 设计思路：
 * - 使用 Caffeine Cache 存储每个 key 对应的 ReentrantLock 对象，确保锁对象的唯一性和自动回收。
 * - 提供 withLock 方法，接受一个 key 和一个操作（Supplier 或 Runnable），在锁保护下执行该操作。
 * - 通过 getLock 方法允许用户直接获取底层锁对象，以便进行更复杂的锁操作（如条件等待）。
 */
public class StripedLock<K> {

    private static final Function<Object, ReentrantLock> LOCK_FACTORY = _ -> new ReentrantLock();

    private final Cache<K, ReentrantLock> lockCache;

    public StripedLock() {
        this.lockCache = Caffeine.newBuilder().weakValues().build(_ -> new ReentrantLock());
    }

    /**
     * 在锁保护下执行操作（无参数，有返回值）
     */
    public <T> T withLock(K key, @NotNull Supplier<T> action) {
        ReentrantLock lock = lockCache.get(key, LOCK_FACTORY);
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 在锁保护下执行操作（有参数，无返回值）
     */
    public void withLock(K key, @NotNull Consumer<K> action) {
        ReentrantLock lock = lockCache.get(key, LOCK_FACTORY);
        lock.lock();
        try {
            action.accept(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 在锁保护下执行操作（无参数，无返回值）
     */
    public void withLock(K key, @NotNull Runnable action) {
        ReentrantLock lock = lockCache.get(key, LOCK_FACTORY);
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取底层锁对象（高级用法）
     */
    public ReentrantLock getLock(K key) {
        return lockCache.get(key, LOCK_FACTORY);
    }

}
