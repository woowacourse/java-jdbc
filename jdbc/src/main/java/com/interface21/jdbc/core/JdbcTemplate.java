package com.interface21.jdbc.core;

import com.interface21.jdbc.JdbcTypeMapper;
import com.interface21.jdbc.ResultSetMapper;
import com.interface21.jdbc.SqlExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        execute(
                PreparedStatement::executeUpdate,
                sql,
                params
        );
    }

    public Optional<Object> queryForObject(
            final String sql,
            final ResultSetMapper<?> mapper,
            final Object... params
    ) {
        return execute(
                (pstmt) -> {
                    return getQueryResult((resultSet -> {
                        if (resultSet.next()) {
                            return Optional.of(mapper.map(resultSet));
                        }
                        return Optional.empty();
                    }), pstmt);
                },
                sql,
                params
        );
    }

    public List<Object> queryForList(
            final String sql,
            final ResultSetMapper<?> mapper,
            final Object... params
    ) {
        return execute(
                (pstmt) -> {
                    return getQueryResult((resultSet -> {
                        List<Object> results = new ArrayList<>();
                        while (resultSet.next()) {
                            results.add(mapper.map(resultSet));
                        }
                        return results;
                    }), pstmt);
                },
                sql,
                params
        );
    }

    private <R> R execute(
            final SqlExecution<PreparedStatement, R> execution,
            final String sql,
            final Object ... params
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameters(params, pstmt);

            return execution.apply(pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <R> R getQueryResult(final SqlExecution<ResultSet, R> queryResultMapping, final PreparedStatement pstmt) throws SQLException{
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            return queryResultMapping.apply(resultSet);
        }
    }

    private void setParameters(final Object[] params, final PreparedStatement pstmt) throws SQLException {
        if (params.length == 0) {
            return;
        }

        for (int idx = 1; idx <= params.length; idx++) {
            Object param = params[idx - 1];

            JdbcTypeMapper mapper = JdbcTypeMapper.fromClassType(param);
            mapper.map(pstmt, idx, param);
        }
    }
}
