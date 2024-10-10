package com.interface21.jdbc.core;

import com.interface21.jdbc.exception.JdbcQueryException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int executeUpdate(String sql, Object... values) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setQueryParameter(preparedStatement, values);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcQueryException("executeUpdate 메서드 실패 : " + e.getMessage(), e);
        }
    }

    private void setQueryParameter(PreparedStatement statement, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            statement.setObject(i + 1, values[i]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setQueryParameter(preparedStatement, values);
            ResultSet rs = preparedStatement.executeQuery();
            return collectResultSet(rs, rowMapper, sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcQueryException("query 메서드 실패 : " + e.getMessage(), e);
        }
    }

    private <T> List<T> collectResultSet(ResultSet resultSet, RowMapper<T> rowMapper, String sql) throws SQLException {
        List<T> resultSets = new ArrayList<>();
        log.debug("query : {}", sql);
        while (resultSet.next()) {
            resultSets.add(rowMapper.mapRow(resultSet));
        }
        return resultSets;
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        List<T> resultSets = query(sql, rowMapper, values);
        try {
            return Optional.ofNullable(resultSets.getFirst());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }
}
