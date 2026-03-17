/*
 * Data module auto configuration
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.common.config;

import me.pectics.kernelclaude.data.common.database.SchemaInitializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Auto-configuration for the data module's common infrastructure.
 */
@AutoConfiguration
@EnableConfigurationProperties(DataProperties.class)
public class DataAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SchemaInitializer schemaInitializer(DataSource dataSource, DataProperties properties) {
        return new SchemaInitializer(dataSource, properties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "dataExecutorService")
    public ExecutorService dataExecutorService(DataProperties properties) {
        return Executors.newFixedThreadPool(
                properties.getThreadPoolSize(),
                new DataThreadFactory()
        );
    }

    /**
     * Thread factory for data operations.
     */
    private static class DataThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r, "data-executor-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }

}
