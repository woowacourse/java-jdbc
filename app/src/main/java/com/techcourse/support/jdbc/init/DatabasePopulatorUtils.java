package com.techcourse.support.jdbc.init;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);
    private static final String DEFAULT_SCHEMA_NAME = "schema.sql";

    private DatabasePopulatorUtils() {
    }

    public static void execute(final DataSource dataSource) {
        execute(dataSource, DEFAULT_SCHEMA_NAME);
    }

    public static void execute(final DataSource dataSource, final String fileName) {
        final String sql = readStringByFileName(fileName);
        try (
            final Connection connection = dataSource.getConnection();
            final Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String readStringByFileName(final String fileName) {
        final var url = DatabasePopulatorUtils.class.getClassLoader().getResource(fileName);
        try {
            final var file = new File(Objects.requireNonNull(url).getFile());
            return Files.readString(file.toPath());
        } catch (NullPointerException | IOException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(fileName + " not found");
        }
    }
}
