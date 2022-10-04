package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);
    private static final String SCHEMA_FILE_NAME = "schema.sql";

    private DatabasePopulatorUtils() {}

    public static void execute(final DataSource dataSource) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()
        ) {
            final var sql = readSql();
            statement.execute(sql);

        } catch (final NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String readSql() throws IOException {
        final var url = getURL();
        final var file = new File(url.getFile());
        return Files.readString(file.toPath());
    }

    private static URL getURL() {
        final var clazz = DatabasePopulatorUtils.class;
        return clazz.getClassLoader()
                .getResource(SCHEMA_FILE_NAME);
    }
}
