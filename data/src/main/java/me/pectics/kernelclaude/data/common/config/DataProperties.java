/*
 * Data module configuration properties
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.common.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the data module.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kernel-claude.data")
public class DataProperties {

    /**
     * Table prefix for all database tables.
     */
    private @NotNull String tablePrefix = "kc_";

    /**
     * Whether to automatically initialize schemas.
     */
    private boolean autoInitializeSchema = true;

    /**
     * Thread pool size for async operations.
     */
    private int threadPoolSize = 4;

}
