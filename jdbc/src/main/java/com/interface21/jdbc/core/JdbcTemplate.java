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

    public int executeUpdate(String sql, Object... args) {
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(args);
        return execute(sql, argumentPreparedStatementSetter, PreparedStatement::executeUpdate);
    }

    private <T> T execute(String sql, PreparedStatementSetter preparedStatementSetter, Callback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            preparedStatementSetter.setValues(preparedStatement);
            return callback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcQueryException("execute 메서드 실패 : " + e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = new ArgumentPreparedStatementSetter(args);
        return execute(sql, argumentPreparedStatementSetter, preparedStatement -> query(preparedStatement, rowMapper));
    }

    private <T> List<T> query(PreparedStatement preparedStatement, RowMapper<T> rowMapper)
            throws SQLException {
        ResultSet rs = preparedStatement.executeQuery();
        return collectResultSet(rs, rowMapper);
    }

    private <T> List<T> collectResultSet(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> resultSets = new ArrayList<>();
        while (resultSet.next()) {
            resultSets.add(rowMapper.mapRow(resultSet));
        }
        return resultSets;
    }

    public <T> Optional<T> queryForObject(String sql,
                                          RowMapper<T> rowMapper,
                                          Object... args) {
        List<T> resultSets = query(sql, rowMapper, args);
        try {
            return Optional.ofNullable(resultSets.getFirst());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }
}
