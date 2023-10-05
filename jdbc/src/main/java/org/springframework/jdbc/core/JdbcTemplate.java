package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int MAX_RESULT_SIZE = 1;

    private QueryExecutorService queryExecutorService;

    public JdbcTemplate(DataSource dataSource) {
        this.queryExecutorService = new QueryExecutorService(dataSource);
    }

    public int update(String sql, Object... args) {
        return queryExecutorService.execute(PreparedStatement::executeUpdate, sql, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {

        return queryExecutorService.execute(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }, sql, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {

        List<T> results = queryExecutorService.execute(preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> execute = new ArrayList<>();
            while (resultSet.next()) {
                execute.add(rowMapper.mapRow(resultSet));
            }
            return execute;
        }, sql, args);

        validate(results);

        return results.iterator().next();
    }

    private static <T> void validate(List<T> results) {
        if (results.isEmpty()) {
            throw new NoSuchElementException("Data Row가 빈 값입니다.");
        }
        if (results.size() > MAX_RESULT_SIZE) {
            throw new IncorrectResultSizeException("적합한 ResultSize를 초과했습니다.");
        }
    }
}
