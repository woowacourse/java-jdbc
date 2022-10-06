package com.techcourse.support.jdbc.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);
    private static final String DEFAULT_FILE_NAME = "schema.sql";

    private DatabasePopulatorUtils() {
    }

    public static void execute(final DataSource dataSource) {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            var sql = readFileToString(DEFAULT_FILE_NAME);
            statement.execute(sql);
        } catch (SQLException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String readFileToString(final String fileName) throws IOException {
        var url = DatabasePopulatorUtils.class.getClassLoader().getResource(fileName);
        var file = new File(url.getFile());
        return Files.readString(file.toPath());
    }
}
