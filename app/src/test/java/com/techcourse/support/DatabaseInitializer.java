package com.techcourse.support;

import com.interface21.jdbc.core.JdbcTemplate;
import java.util.List;

public class DatabaseInitializer {

    public static final String TRUNCATE_FORMAT = "TRUNCATE TABLE %s";
    public static final String SET_FOREIGN_KEY_CHECKS = "SET REFERENTIAL_INTEGRITY %s;";
    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void truncateAll(final String databaseName) {
        final List<String> tableNames = getTableNames(databaseName);
        jdbcTemplate.update(String.format(SET_FOREIGN_KEY_CHECKS, "FALSE"));
        tableNames.forEach(name -> jdbcTemplate.update(String.format(TRUNCATE_FORMAT, name)));
        jdbcTemplate.update(String.format(SET_FOREIGN_KEY_CHECKS, "TRUE"));
    }

    private List<String> getTableNames(final String databaseName) {
        final String queryTableNames = "SHOW TABLES FROM %s";
        return jdbcTemplate.query(String.format(queryTableNames, databaseName), rs -> rs.getString(1));
    }
}
