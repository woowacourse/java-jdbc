package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return withConnection(connection -> update(connection, sql, parameters));
    }

    /** 전달받은 연결은 호출자가 닫는다. */
    public int update(final Connection connection, final String sql, final Object... parameters) {
        return execute(connection, sql, parameters, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return withConnection(connection -> query(connection, sql, rowMapper, parameters));
    }

    private <T> List<T> query(final Connection connection, final String sql,
                             final RowMapper<T> rowMapper, final Object[] parameters) {
        return execute(connection, sql, parameters, statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                final List<T> rows = new ArrayList<>();
                while (resultSet.next()) {
                    rows.add(rowMapper.mapRow(resultSet));
                }
                return rows;
            }
        });
    }

    /**
     * 결과가 없으면 null을, 한 건이면 매핑한 객체를 반환한다.
     * 두 건 이상이면 DataAccessException을 던진다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return singleResult(query(sql, rowMapper, parameters));
    }

    /** 전달받은 연결은 호출자가 닫는다. */
    public <T> T queryForObject(final Connection connection, final String sql,
                                final RowMapper<T> rowMapper, final Object... parameters) {
        return singleResult(query(connection, sql, rowMapper, parameters));
    }

    private <T> T singleResult(final List<T> rows) {
        if (rows.isEmpty()) {
            return null;
        }
        if (rows.size() > 1) {
            throw new DataAccessException("단건 조회에서 %d건이 반환되었습니다.".formatted(rows.size()));
        }
        return rows.getFirst();
    }

    private <T> T withConnection(final Function<Connection, T> operation) {
        try (Connection connection = dataSource.getConnection()) {
            return operation.apply(connection);
        } catch (SQLException exception) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", exception);
        }
    }

    private <T> T execute(final Connection connection, final String sql,
                          final Object[] parameters, final StatementCallback<T> callback) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, parameters);
            log.debug("query : {}", sql);
            return callback.execute(statement);
        } catch (SQLException exception) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", exception);
        }
    }

    private void setParameters(final PreparedStatement statement, final Object[] parameters) throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            statement.setObject(index + 1, parameters[index]);
        }
    }

    @FunctionalInterface
    private interface StatementCallback<T> {

        T execute(PreparedStatement statement) throws SQLException;
    }
}
