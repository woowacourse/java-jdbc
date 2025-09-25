package com.interface21.jdbc.client;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.ResultExtractor;
import java.util.List;
import javax.sql.DataSource;

public class Sql {

    private final JdbcTemplate jdbcTemplate;

    public Sql(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Sql(DataSource dataSource) {
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
        // TODO : 구현
    }

    public void update(Update update) {
        // TODO : 구현
    }

    public <T> List<T> selectMany(Select select, ResultExtractor<T> extractor) {
        // TODO : 구현
        return null;
    }

    public <T> T selectOne(Select select, ResultExtractor<T> extractor) {
        // TODO : 구현
        return null;
    }
}
