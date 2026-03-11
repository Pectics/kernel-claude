package me.pectics.kernelclaude.permission.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.pectics.kernelclaude.permission.GroupManager;
import me.pectics.kernelclaude.permission.User;
import me.pectics.kernelclaude.permission.UserManager;
import me.pectics.kernelclaude.permission.UserRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserManager 的简单实现
 * <p>
 * 带内存缓存层的用户管理器
 */
@RequiredArgsConstructor
public class SimpleUserManager implements UserManager {

    private final UserRepository userRepository;
    private final GroupManager groupManager;

    /**
     * 用户缓存
     * Key: 用户 ID (platform:nativeId)
     */
    private final Map<String, User> userCache = new ConcurrentHashMap<>();

    @Override
    public @NotNull Optional<User> getUser(@NonNull String id) {
        return Optional.ofNullable(
                userCache.computeIfAbsent(id, key ->
                        userRepository.find(key).orElse(null)
                )
        );
    }

    @Override
    public @NotNull Optional<User> getUser(@NonNull String platform, @NonNull String nativeId) {
        String id = platform + ":" + nativeId;
        return getUser(id);
    }

    @Override
    public @NotNull Collection<User> getUsersByPlatform(@NonNull String platform) {
        return userRepository.findByPlatform(platform);
    }

    @Override
    public @NotNull Collection<User> getUsersByGroup(@NonNull String groupId) {
        return userRepository.findByGroup(groupId);
    }

    @Override
    public @NotNull User createUser(@NonNull String platform, @NonNull String nativeId) {
        String id = platform + ":" + nativeId;

        if (hasUser(platform, nativeId)) {
            throw new IllegalArgumentException("User already exists: " + id);
        }

        // 创建新用户
        SimpleUser user = new SimpleUser(platform, nativeId);
        injectDependencies(user);

        // 保存到仓库
        userRepository.save(user);

        // 加入缓存
        userCache.put(id, user);

        return user;
    }

    @Override
    public boolean deleteUser(@NonNull String id) {
        if (!id.contains(":")) {
            return false;
        }
        String[] parts = id.split(":", 2);
        return deleteUser(parts[0], parts[1]);
    }

    @Override
    public boolean deleteUser(@NonNull String platform, @NonNull String nativeId) {
        String id = platform + ":" + nativeId;

        if (!hasUser(platform, nativeId)) {
            return false;
        }

        // 从仓库删除
        userRepository.delete(platform, nativeId);

        // 从缓存移除
        userCache.remove(id);

        return true;
    }

    @Override
    public boolean hasUser(@NonNull String id) {
        if (userCache.containsKey(id)) {
            return true;
        }
        return userRepository.find(id).isPresent();
    }

    @Override
    public boolean hasUser(@NonNull String platform, @NonNull String nativeId) {
        String id = platform + ":" + nativeId;
        return hasUser(id);
    }

    /**
     * 保存用户到仓库并更新缓存
     *
     * @param user 用户对象
     */
    public void saveUser(@NonNull User user) {
        userRepository.save(user);
        userCache.put(user.getId(), user);
    }

    /**
     * 刷新缓存中的用户数据
     *
     * @param id 用户 ID
     */
    public void refreshUser(@NonNull String id) {
        userCache.remove(id);
        getUser(id);
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        userCache.clear();
    }

    /**
     * 向用户对象注入依赖
     */
    private void injectDependencies(SimpleUser user) {
        user.setGroupManager(groupManager);
        // PermissionCalculator 可以在需要时注入
    }
}
