package com.techcourse.dao;

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

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public final void update(final String sql, final Object... args) {
        update(sql, statement -> bindParameters(statement, args));
    }

    public final void update(final String sql, final PreparedStatementSetter setter) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            if (setter != null) {
                setter.setValues(statement);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    public final <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, statement -> bindParameters(statement, args), rowMapper);
    }

    public final <T> List<T> query(final String sql, final PreparedStatementSetter setter,
                                 final RowMapper<T> rowMapper) {
        try (Connection connection = getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            final List<T> results = new ArrayList<>();
            try (ResultSet resultSet = executeQuery(setter, statement)) {
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다: " + sql, e);
        }
    }

    public final <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        return results.isEmpty() ? null : results.getFirst();
    }

    private ResultSet executeQuery(final PreparedStatementSetter setter, final PreparedStatement statement)
            throws SQLException {
        if (setter != null) {
            setter.setValues(statement);
        }
        return statement.executeQuery();
    }

    private static void bindParameters(final PreparedStatement statement, final Object... args)
            throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }

    protected abstract DataSource getDataSource();
}
