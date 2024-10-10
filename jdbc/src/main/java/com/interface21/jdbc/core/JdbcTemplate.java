package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        return executeQuery(sql, setParameter(params), PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        log.debug("queryForObject Executing SQL: {}", sql);

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameter(preparedStatement, params);
            final ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs);
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
                values.add(rowMapper.mapRow(rs));
            }
            return values;
        } catch (final SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //TODO 줄일건지 말건지 하나만 하기
    private <T> T executeQuery(final String sql, final PreparedStatementSetter pss, final SqlExecutor<T> executor) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValue(ps);
            return executor.executor(ps);
        } catch (final SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private PreparedStatementSetter setParameter(final Object... params) {
        final AtomicInteger index = new AtomicInteger(1);
        //TODO 너무 후짐
        return ps -> {
            for (final Object param : params) {
                ps.setObject(index.getAndIncrement(), param);
            }
        };
    }
}
