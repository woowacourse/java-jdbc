package com.techcourse.support.jdbc.init;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(final DataSource dataSource) {
        Connection connection = null;
        Statement statement = null;
        InputStream inputStream = null;
        try {
            inputStream = DatabasePopulatorUtils.class.getClassLoader().getResourceAsStream("schema.sql");
            if (inputStream == null) {
                throw new IllegalStateException("schema.sql이 없습니다.");
            }
            final var sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
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
            } catch (SQLException ignored) {}

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ignored) {}
        }
    }

    private DatabasePopulatorUtils() {}
}
