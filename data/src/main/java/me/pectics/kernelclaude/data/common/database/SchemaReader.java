/*
 * SQL schema script reader
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.common.database;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and parses SQL schema files into individual statements.
 */
public final class SchemaReader {

    private SchemaReader() {
        // Utility class
    }

    /**
     * Reads SQL statements from an input stream.
     *
     * @param inputStream the input stream
     * @return a list of SQL statements
     * @throws IOException if reading fails
     */
    public static @NotNull List<String> readStatements(@NotNull InputStream inputStream) throws IOException {
        List<String> statements = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder currentStatement = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comments
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--"))
                    continue;

                // Append to current statement
                if (!currentStatement.isEmpty())
                    currentStatement.append(" ");
                currentStatement.append(trimmed);

                // Check if statement is complete (ends with semicolon)
                if (trimmed.endsWith(";")) {
                    String statement = currentStatement.toString();
                    // Remove trailing semicolon
                    statement = statement.substring(0, statement.length() - 1).trim();
                    if (!statement.isEmpty())
                        statements.add(statement);
                    currentStatement = new StringBuilder();
                }
            }

            // Handle last statement without semicolon
            String lastStatement = currentStatement.toString().trim();
            if (!lastStatement.isEmpty())
                statements.add(lastStatement);
        }

        return statements;
    }

    /**
     * Applies a statement processor to all statements.
     *
     * @param statements the statements to process
     * @param processor  the processor to apply
     * @return the processed statements
     */
    public static @NotNull List<String> processStatements(
            @NotNull List<String> statements,
            @NotNull StatementProcessor processor) {
        List<String> processed = new ArrayList<>(statements.size());
        for (String statement : statements)
            processed.add(processor.process(statement));
        return processed;
    }

    /**
     * Replaces placeholders in statements with actual values.
     *
     * @param statements    the statements
     * @param tablePrefix   the table prefix
     * @return the statements with replaced placeholders
     */
    public static @NotNull List<String> replacePlaceholders(
            @NotNull List<String> statements,
            @NotNull String tablePrefix) {
        List<String> result = new ArrayList<>(statements.size());
        for (String statement : statements) {
            String replaced = statement.replace("{prefix}", tablePrefix);
            result.add(replaced);
        }
        return result;
    }

}
