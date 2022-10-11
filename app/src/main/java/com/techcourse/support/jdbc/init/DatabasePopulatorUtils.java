package com.techcourse.support.jdbc.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DatabasePopulatorUtils {

    private static final Logger log = LoggerFactory.getLogger(DatabasePopulatorUtils.class);

    public static void execute(final DataSource dataSource, final String ddl) {
        try(final var connection = dataSource.getConnection();
            final var statement = connection.createStatement();
            final var stream = DatabasePopulatorUtils.class.getClassLoader().getResourceAsStream(ddl)
        ) {
            final var sql = new String(stream.readAllBytes());
            log.info("execute sql : {}", ddl);
            statement.execute(sql);
        } catch (NullPointerException | IOException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DatabasePopulatorUtils() {}
}
