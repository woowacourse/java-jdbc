package com.techcourse.support.jdbc.init;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class DatabasePopulatorUtils {

    public static void execute(final DataSource dataSource) {
        try (final var input = DatabasePopulatorUtils.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (input == null) {
                throw new IOException("schema.sql was not found on the classpath");
            }
            final var sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (final var connection = dataSource.getConnection();
                 final var statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (IOException | SQLException e) {
            throw new DataAccessException("Failed to initialize the database", e);
        }
    }

    private DatabasePopulatorUtils() {}
}
