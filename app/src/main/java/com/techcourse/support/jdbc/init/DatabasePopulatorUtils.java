package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(final DataSource dataSource) {
        Connection connection = null;
        Statement statement = null;
        try {
            final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final var file = new File(url.getFile());
            final var sql = Files.readString(file.toPath());
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public static void cleanUp(final DataSource dataSource) {
        Connection connection = null;
        Statement statement = null;
        try {
            String sql = """
                            SET REFERENTIAL_INTEGRITY FALSE;
                            TRUNCATE TABLE users;
                            TRUNCATE TABLE user_history;
                            ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
                            ALTER TABLE user_history ALTER COLUMN id RESTART WITH 1;
                            SET REFERENTIAL_INTEGRITY TRUE;
                    """;

            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (NullPointerException | SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private DatabasePopulatorUtils() {
    }
}
