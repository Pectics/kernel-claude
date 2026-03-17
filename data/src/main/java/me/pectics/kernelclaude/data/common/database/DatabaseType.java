/*
 * Database type enumeration for multi-database support
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.common.database;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Represents supported database types with their specific SQL syntax.
 */
public enum DatabaseType {

    MYSQL("`", "INT AUTO_INCREMENT", "utf8mb4"),
    MARIADB("`", "INT AUTO_INCREMENT", "utf8mb4"),
    POSTGRESQL("\"", "SERIAL", "UTF8"),
    SQLITE("`", "INTEGER PRIMARY KEY AUTOINCREMENT", "UTF-8"),
    H2("`", "INT AUTO_INCREMENT", "UTF8");

    private final String identifierQuote;
    private final String autoIncrementSyntax;
    private final String defaultCharset;

    DatabaseType(String identifierQuote, String autoIncrementSyntax, String defaultCharset) {
        this.identifierQuote = identifierQuote;
        this.autoIncrementSyntax = autoIncrementSyntax;
        this.defaultCharset = defaultCharset;
    }

    /**
     * Gets the quote character used for identifiers.
     *
     * @return the identifier quote character
     */
    public @NotNull String getIdentifierQuote() {
        return identifierQuote;
    }

    /**
     * Gets the auto-increment syntax for this database.
     *
     * @return the auto-increment syntax
     */
    public @NotNull String getAutoIncrementSyntax() {
        return autoIncrementSyntax;
    }

    /**
     * Gets the default charset for this database.
     *
     * @return the default charset
     */
    public @NotNull String getDefaultCharset() {
        return defaultCharset;
    }

    /**
     * Creates a statement processor for this database type.
     *
     * @return the statement processor
     */
    public @NotNull StatementProcessor getStatementProcessor() {
        return switch (this) {
            case POSTGRESQL -> StatementProcessor.doubleQuotes();
            default -> StatementProcessor.backticks();
        };
    }

    /**
     * Detects the database type from a JDBC URL.
     *
     * @param jdbcUrl the JDBC URL
     * @return the detected database type
     * @throws IllegalArgumentException if the URL is not recognized
     */
    public static @NotNull DatabaseType fromJdbcUrl(@NotNull String jdbcUrl) {
        String lowerUrl = jdbcUrl.toLowerCase(Locale.ROOT);

        if (lowerUrl.startsWith("jdbc:mysql:"))
            return MYSQL;
        if (lowerUrl.startsWith("jdbc:mariadb:"))
            return MARIADB;
        if (lowerUrl.startsWith("jdbc:postgresql:"))
            return POSTGRESQL;
        if (lowerUrl.startsWith("jdbc:sqlite:"))
            return SQLITE;
        if (lowerUrl.startsWith("jdbc:h2:"))
            return H2;

        throw new IllegalArgumentException("Unknown database type for JDBC URL: " + jdbcUrl);
    }

    /**
     * Checks if this database supports ON CONFLICT DO NOTHING syntax.
     *
     * @return true if supported
     */
    public boolean supportsOnConflictDoNothing() {
        return this == POSTGRESQL || this == SQLITE;
    }

    /**
     * Checks if this database supports MERGE INTO syntax.
     *
     * @return true if supported
     */
    public boolean supportsMergeInto() {
        return this == H2;
    }

}
