package com.techcourse.support.jdbc.init;

import com.interface21.dao.DataAccessException;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class DatabasePopulatorUtils {
    public static void execute(final DataSource dataSource) {
        try (var schema = DatabasePopulatorUtils.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (schema == null) {
                throw new IllegalStateException("schema.sql was not found");
            }
            final var sql = new String(schema.readAllBytes(), StandardCharsets.UTF_8);
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (IOException | SQLException e) {
            throw new DataAccessException("Failed to initialize database schema", e);
        }
    }

    private DatabasePopulatorUtils() {}
}
