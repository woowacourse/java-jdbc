package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(final DataSource dataSource) {
        execute(dataSource, "schema.sql");
    }

    public static void clear(final DataSource dataSource) {
        execute(dataSource, "truncate.sql");
    }

    private static void execute(final DataSource dataSource, final String resource) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            
            final var url = DatabasePopulatorUtils.class.getClassLoader().getResource(resource);
            final var file = new File(url.getFile());
            final var sql = Files.readString(file.toPath());

            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage());
        }
    }

    private DatabasePopulatorUtils() {
    }
}
