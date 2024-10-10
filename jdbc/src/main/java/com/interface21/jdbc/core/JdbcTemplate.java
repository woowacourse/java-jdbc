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

    public int executeUpdate(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcQueryException("executeUpdate 메서드 실패 : " + e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
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

    public <T> Optional<T> queryForObject(String sql,
                                          PreparedStatementSetter preparedStatementSetter,
                                          RowMapper<T> rowMapper) {
        List<T> resultSets = query(sql, preparedStatementSetter, rowMapper);
        try {
            return Optional.ofNullable(resultSets.getFirst());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }
}
