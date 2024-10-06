package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        log.debug("update Executing SQL: {}", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameter(preparedStatement, params);
            return preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        log.debug("queryForObject Executing SQL: {}", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameter(preparedStatement, params);
            final ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs, 1);
            }
            return null;
        } catch (final SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        log.debug("queryForList Executing SQL: {}", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameter(preparedStatement, params);

            final ResultSet rs = preparedStatement.executeQuery();
            final List<T> values = new ArrayList<>();
            while (rs.next()) {
                values.add(rowMapper.mapRow(rs, rs.getFetchSize()));
            }
            return values;
        } catch (final SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void setParameter(final PreparedStatement preparedStatement, final Object... params) throws SQLException {
        int index = 1;
        for (final Object param : params) {
            preparedStatement.setObject(index++, param);
        }
    }
}
