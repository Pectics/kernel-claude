package me.pectics.kernelclaude.data.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Data 模块配置类
 */
@Configuration
@MapperScan("me.pectics.kernelclaude.data.mapper")
public class DataAutoConfiguration {
}
