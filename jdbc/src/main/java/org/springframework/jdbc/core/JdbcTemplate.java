package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_RESULT_INDEX = 0;

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setObjects(args, preparedStatement);

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.info("update error: {}", e.getMessage());
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setObjects(Object[] args, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            setObjects(args, preparedStatement);

            return getResults(rowMapper, preparedStatement);
        } catch (SQLException e) {
            log.info("query error: {}", e.getMessage());
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> getResults(RowMapper<T> rowMapper, PreparedStatement preparedStatement) throws SQLException {
        List<T> results = new ArrayList<>();
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            T result = rowMapper.run(rs);
            results.add(result);
        }

        return results;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        T t = results.get(FIRST_RESULT_INDEX);
        if (t == null) {
            throw new IndexOutOfBoundsException("queryForObject error: empty result");
        }
        return t;
    }
}
