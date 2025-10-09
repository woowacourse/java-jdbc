package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            setParams(statement, params);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL 실행 실패: {} 파라미터: {}", sql, List.of(params), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql);
             final ResultSet resultSet = executeQuery(statement, params);
        ) {
            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            log.error("SQL 실행 실패: {} 파라미터: {}", sql, List.of(params), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql);
             final ResultSet resultSet = executeQuery(statement, params);
        ) {
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error("SQL 실행 실패: {} 파라미터: {}", sql, List.of(params), e);
            throw new RuntimeException(e);
        }
    }

    private void setParams(final PreparedStatement statement, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; ++i) {
            statement.setObject(i + 1, params[i]);
        }
    }

    private ResultSet executeQuery(final PreparedStatement statement, final Object... params) throws SQLException {
        setParams(statement, params);
        return statement.executeQuery();
    }
}
