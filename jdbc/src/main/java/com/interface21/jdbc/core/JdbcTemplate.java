package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public int update(final String sql, final Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, new ArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        return execute(sql, setter, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException(failureMessage("단건 조회 결과가 여러 건입니다(" + results.size() + "건)", sql));
        }
        return results.stream().findFirst();
    }

    private <T> T execute(final String sql, final PreparedStatementSetter setter, final StatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setter.setValues(statement);
            return callback.doInStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException(failureMessage("SQL 실행에 실패했습니다", sql), e);
        }
    }

    private String failureMessage(final String reason, final String sql) {
        return reason + ": " + sql;
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T doInStatement(PreparedStatement statement) throws SQLException;
    }
}
