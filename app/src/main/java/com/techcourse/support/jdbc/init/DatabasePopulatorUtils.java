package com.techcourse.support.jdbc.init;

import com.interface21.jdbc.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(final DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();

        try (Connection connection = dataSource.getConnection()) {
            final var url = DatabasePopulatorUtils.class.getClassLoader().getResource("schema.sql");
            final var file = new File(url.getFile());
            final var sql = Files.readString(file.toPath());
            jdbcTemplate.execute(connection, sql);
        } catch (NullPointerException | IOException | DataAccessException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DatabasePopulatorUtils() {
    }
}
