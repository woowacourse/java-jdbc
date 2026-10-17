package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            bindParameters(statement, args);

            try (final var resultSet = statement.executeQuery()) {
                final var results = new ArrayList<T>();

                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }

                return List.copyOf(results);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args) {
        final var results = query(sql, rowMapper, args);

        if (results.size() > 1) { // 동일한 account가 존재하는 것을 예외로 잡지 않고 그냥 첫 사람으로 return 했습니다. 이유는 테스트코드가 그래서
            return results.getFirst();
        }

        if (results.isEmpty()) {
            return null;
        }

        return results.getFirst();
    }

    public int update(final String sql, final Object... args) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)
        ) {
            bindParameters(statement, args);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void bindParameters(
            final PreparedStatement statement,
            final Object[] args
            ) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
