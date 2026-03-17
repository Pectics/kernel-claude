/*
 * Database schema initializer
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.common.database;

import lombok.extern.slf4j.Slf4j;
import me.pectics.kernelclaude.data.common.config.DataProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Initializes database schemas for different modules.
 */
@Slf4j
public class SchemaInitializer {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final DataProperties properties;
    private final DatabaseType databaseType;

    public SchemaInitializer(DataSource dataSource, DataProperties properties) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.properties = properties;
        this.databaseType = detectDatabaseType();
    }

    /**
     * Gets the detected database type.
     *
     * @return the database type
     */
    public @NotNull DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Initializes the schema for a specific module.
     *
     * @param module         the module name (e.g., "perms")
     * @param schemaLocation the classpath location of schema files
     */
    @Transactional
    public void initializeSchema(@NotNull String module, @NotNull String schemaLocation) {
        log.info("Initializing schema for module: {}", module);

        // Get existing tables
        Set<String> existingTables = getExistingTables();

        // Load and process schema
        String schemaFile = schemaLocation + "/" + getSchemaFileName();
        List<String> statements = loadSchema(schemaFile);

        if (statements.isEmpty()) {
            log.warn("No schema statements found for module: {}", module);
            return;
        }

        // Process statements
        StatementProcessor processor = databaseType.getStatementProcessor();
        statements = SchemaReader.processStatements(statements, processor);
        statements = SchemaReader.replacePlaceholders(statements, properties.getTablePrefix());

        // Execute statements
        for (String statement : statements) {
            try {
                // Skip if table already exists
                String tableName = extractTableName(statement);
                if (tableName != null && existingTables.contains(tableName.toLowerCase())) {
                    log.debug("Table {} already exists, skipping", tableName);
                    continue;
                }

                log.debug("Executing: {}", statement);
                jdbcTemplate.execute(statement);
            } catch (Exception e) {
                log.error("Failed to execute statement: {}", statement, e);
                throw new RuntimeException("Failed to initialize schema for module: " + module, e);
            }
        }

        log.info("Schema initialization completed for module: {}", module);
    }

    /**
     * Gets all existing table names.
     *
     * @return a set of table names (lowercase)
     */
    public @NotNull Set<String> getExistingTables() {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName != null)
                        tables.add(tableName.toLowerCase());
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get existing tables", e);
        }
        return tables;
    }

    /**
     * Detects the database type from the data source.
     *
     * @return the database type
     */
    private @NotNull DatabaseType detectDatabaseType() {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            return DatabaseType.fromJdbcUrl(url);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to detect database type", e);
        }
    }

    /**
     * Gets the schema file name for the current database type.
     *
     * @return the schema file name
     */
    private @NotNull String getSchemaFileName() {
        return switch (databaseType) {
            case MYSQL -> "mysql.sql";
            case MARIADB -> "mariadb.sql";
            case POSTGRESQL -> "postgresql.sql";
            case SQLITE -> "sqlite.sql";
            case H2 -> "h2.sql";
        };
    }

    /**
     * Loads schema statements from a classpath resource.
     *
     * @param location the classpath location
     * @return the list of statements
     */
    private @NotNull List<String> loadSchema(@NotNull String location) {
        ClassPathResource resource = new ClassPathResource(location);
        if (!resource.exists()) {
            log.warn("Schema file not found: {}", location);
            return List.of();
        }

        try (InputStream is = resource.getInputStream()) {
            return SchemaReader.readStatements(is);
        } catch (IOException e) {
            log.error("Failed to load schema file: {}", location, e);
            return List.of();
        }
    }

    /**
     * Extracts the table name from a CREATE TABLE statement.
     *
     * @param statement the SQL statement
     * @return the table name, or null if not a CREATE TABLE statement
     */
    private String extractTableName(String statement) {
        String upper = statement.toUpperCase();
        if (!upper.startsWith("CREATE TABLE"))
            return null;

        // Find the table name after CREATE TABLE
        int start = "CREATE TABLE".length();
        String remainder = statement.substring(start).trim();

        // Handle IF NOT EXISTS
        if (remainder.toUpperCase().startsWith("IF NOT EXISTS")) {
            remainder = remainder.substring("IF NOT EXISTS".length()).trim();
        }

        // Extract table name (first identifier)
        StringBuilder tableName = new StringBuilder();
        for (int i = 0; i < remainder.length(); i++) {
            char c = remainder.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_')
                tableName.append(c);
            else if (c == '`' || c == '"' || c == '\'')
                continue;
            else
                break;
        }

        return tableName.length() > 0 ? tableName.toString() : null;
    }

}
