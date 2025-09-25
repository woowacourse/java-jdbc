package com.interface21.jdbc.dsl;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ResultExtractor;
import java.util.List;
import javax.sql.DataSource;

public class DslJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public DslJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DslJdbcTemplate(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public Insert insert(String table) {
        return new Insert(this, table);
    }

    public Update update(String table) {
        return new Update(this, table);
    }

    public Select select(String... rowNames) {
        return new Select(this, rowNames);
    }

    public void insert(Insert insert) {
        jdbcTemplate.update(insert.toString());
    }

    public void update(Update update) {
        jdbcTemplate.update(update.toString());
    }

    public <T> List<T> selectMany(Select select, ResultExtractor<T> extractor) {
        return jdbcTemplate.queryMany(select.toString(), extractor);
    }

    public <T> T selectOne(Select select, ResultExtractor<T> extractor) {
        return jdbcTemplate.queryOne(select.toString(), extractor);
    }
}
