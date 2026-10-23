package com.techcourse.support.jdbc.init;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

public final class DatabaseTestUtils {

    public static void reset(final DataSource dataSource) {
        DatabasePopulatorUtils.execute(dataSource);

        try (final var connection = dataSource.getConnection();
             final Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE user_history RESTART IDENTITY");
            statement.execute("TRUNCATE TABLE users RESTART IDENTITY");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reset test database", e);
        }
    }

    private DatabaseTestUtils() {}
}
