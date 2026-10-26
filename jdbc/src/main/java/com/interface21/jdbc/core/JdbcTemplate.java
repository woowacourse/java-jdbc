package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;


    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return query(sql, createSetter(args), rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        return execute(sql, preparedStatementSetter, preparedStatement -> extractRows(preparedStatement, rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return singleResult(query(sql, rowMapper, args));
    }

    public <T> T queryForObject(Connection connection, String sql, RowMapper<T> rowMapper, Object... args) {
        final List<T> rows = execute(connection, sql, createSetter(args),
                preparedStatement -> extractRows(preparedStatement, rowMapper));
        return singleResult(rows);
    }

    private <T> T singleResult(List<T> rows) {
        if (rows.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (rows.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, rows.size());
        }
        return rows.getFirst();
    }

    public int update(String query, Object... args) {
        return update(query, createSetter(args));
    }

    public int update(String sql, PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatementSetter, PreparedStatement::executeUpdate);
    }

    public int update(Connection connection, String query, Object... args) {
        return execute(connection, query, createSetter(args), PreparedStatement::executeUpdate);
    }

    private PreparedStatementSetter createSetter(Object... args) {
        return preparedStatement -> {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
        };
    }

    private <T> T execute(
            String sql,
            PreparedStatementSetter preparedStatementSetter,
            PreparedStatementCallback<T> preparedStatementCallback
    ) {
        try (Connection connection = dataSource.getConnection()) {
            return execute(connection, sql, preparedStatementSetter, preparedStatementCallback);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(
            Connection connection,
            String sql,
            PreparedStatementSetter preparedStatementSetter,
            PreparedStatementCallback<T> preparedStatementCallback
    ) {
        log.debug("query : {}", sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatementCallback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> extractRows(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = preparedStatement.executeQuery()) {
            List<T> rows = new ArrayList<>();

            while (rs.next()) {
                rows.add(rowMapper.mapRow(rs));
            }

            return rows;
        }
    }
}
