package com.techcourse.support.jdbc.init;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            final var path = Paths.get(url.toURI());
            final var sql = Files.readString(path, StandardCharsets.UTF_8);
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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
