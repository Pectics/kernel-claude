/*
 * Auto-configuration for perms storage
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms;

import lombok.extern.slf4j.Slf4j;
import me.pectics.kernelclaude.data.common.config.DataProperties;
import me.pectics.kernelclaude.data.common.database.SchemaInitializer;
import me.pectics.kernelclaude.data.perms.mapper.GroupMapper;
import me.pectics.kernelclaude.data.perms.mapper.GroupNodeMapper;
import me.pectics.kernelclaude.data.perms.mapper.UserMapper;
import me.pectics.kernelclaude.data.perms.mapper.UserNodeMapper;
import me.pectics.kernelclaude.perms.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;

/**
 * Auto-configuration for the perms module's storage layer.
 */
@Slf4j
@AutoConfiguration
@MapperScan(basePackages = "me.pectics.kernelclaude.data.perms.mapper")
public class PermsStorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Storage.class)
    public Storage myBatisPermsStorage(
            UserMapper userMapper,
            UserNodeMapper userNodeMapper,
            GroupMapper groupMapper,
            GroupNodeMapper groupNodeMapper,
            @Qualifier("dataExecutorService") ExecutorService executor,
            SchemaInitializer schemaInitializer,
            DataProperties properties) {

        // Initialize schema if needed
        if (properties.getAutoInitializeSchema()) {
            log.info("Initializing perms schema...");
            schemaInitializer.initializeSchema("perms", "me/pectics/kernelclaude/data/perms/schema");
        }

        MyBatisPermsStorage storage = new MyBatisPermsStorage(
                userMapper, userNodeMapper, groupMapper, groupNodeMapper, executor);

        // Set up group resolver for groups
        storage.setGroupResolver(groupId -> {
            try {
                return storage.loadGroup(groupId).join();
            } catch (Exception e) {
                log.error("Failed to resolve group: {}", groupId, e);
                return null;
            }
        });

        // Set up group resolver for users
        storage.setUserGroupResolver(groupName -> {
            try {
                return storage.loadGroup(groupName).join();
            } catch (Exception e) {
                log.error("Failed to resolve group: {}", groupName, e);
                return null;
            }
        });

        // Set up primary group validator
        storage.setPrimaryGroupValidator((user, groupName) -> {
            // User is member of group if they have the inheritance node
            return user.getNodes().stream()
                    .anyMatch(node -> node.getKey().equalsIgnoreCase("group." + groupName));
        });

        log.info("MyBatisPermsStorage initialized");
        return storage;
    }

}
