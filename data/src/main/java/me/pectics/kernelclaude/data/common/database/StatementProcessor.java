/*
 * SQL statement processor for database dialect handling
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.common.database;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for processing SQL statements to adapt to different database dialects.
 */
@FunctionalInterface
public interface StatementProcessor {

    /**
     * Processes a SQL statement for a specific database dialect.
     *
     * @param sql the original SQL statement
     * @return the processed SQL statement
     */
    @NotNull String process(@NotNull String sql);

    /**
     * Creates a processor that replaces single quotes with backticks.
     * Used for MySQL, MariaDB, SQLite, H2.
     *
     * @return the backtick processor
     */
    static @NotNull StatementProcessor backticks() {
        return sql -> sql.replace('\'', '`');
    }

    /**
     * Creates a processor that replaces single quotes with double quotes.
     * Used for PostgreSQL.
     *
     * @return the double-quote processor
     */
    static @NotNull StatementProcessor doubleQuotes() {
        return sql -> sql.replace('\'', '"');
    }

    /**
     * Creates an identity processor that doesn't modify the SQL.
     *
     * @return the identity processor
     */
    static @NotNull StatementProcessor identity() {
        return sql -> sql;
    }

}
