package com.interface21.jdbc.utils;

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

    private DatabasePopulatorUtils() {
    }

    public static void createTables(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final var file = new File(url.getFile());
            final var sql = Files.readString(file.toPath());
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void truncateTables(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("truncate.sql");
            final var file = new File(url.getFile());
            final var sql = Files.readString(file.toPath());
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
