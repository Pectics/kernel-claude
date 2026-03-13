/*
 * Data Auto Configuration
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.config;

import me.pectics.kernelclaude.data.mapper.GroupMapper;
import me.pectics.kernelclaude.data.mapper.PermissionNodeMapper;
import me.pectics.kernelclaude.data.mapper.UserMapper;
import me.pectics.kernelclaude.data.storage.MyBatisPermsStorage;
import me.pectics.kernelclaude.perms.storage.Storage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Data 模块自动配置类
 */
@Configuration
@MapperScan("me.pectics.kernelclaude.data.mapper")
public class DataAutoConfiguration {

    @Bean
    public Storage permsStorage(UserMapper userMapper,
                                GroupMapper groupMapper,
                                PermissionNodeMapper nodeMapper) {
        return new MyBatisPermsStorage(userMapper, groupMapper, nodeMapper);
    }
}
