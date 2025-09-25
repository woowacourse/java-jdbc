package com.interface21.jdbc.core;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        testConnection(dataSource);
    }

    private void testConnection(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            var databaseProductName = connection.getMetaData().getDatabaseProductName();
            log.info("Connection established to database : {}", databaseProductName);

        } catch (NullPointerException e) {
            log.error("Connection is null on dataSource {}", dataSource);
            throw new RuntimeException(e);

        } catch (SQLException e) {
            log.error(e.getMessage(), e.getCause());
            throw new RuntimeException(e);
        }
    }
}
