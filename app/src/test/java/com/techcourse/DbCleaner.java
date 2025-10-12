package com.techcourse;

import com.interface21.jdbc.JdbcExecutionException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import java.util.List;

public class DbCleaner {

    private final JdbcTemplate jdbcTemplate;

    public DbCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanH2() {
        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY FALSE");

        String sql = """
            SELECT table_name
            FROM INFORMATION_SCHEMA.TABLES
            WHERE table_schema = SCHEMA()
              AND (table_type = 'BASE TABLE' OR table_type = 'TABLE')
              AND LOWER(table_name) <> 'flyway_schema_history'
            """;

        List<String> tables = jdbcTemplate.query(sql, stringMapper());

        for (String table : tables) {
            try {
                jdbcTemplate.update("TRUNCATE TABLE " + table);
            } catch (JdbcExecutionException e) {
                throw e;
            }
        }

        jdbcTemplate.update("ALTER TABLE \"USERS\" ALTER COLUMN \"ID\" RESTART WITH 1");

        jdbcTemplate.update("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private RowMapper<String> stringMapper() {
        return (rs) -> rs.getString(1);
    }
}
