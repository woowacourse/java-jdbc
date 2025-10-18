package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.InvalidResultSetException;
import com.interface21.jdbc.QueryResultMapper;
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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(
            final Connection connection,
            final String sql,
            final Object... params
    ) {
        execute(
                connection,
                PreparedStatement::executeUpdate,
                sql,
                params
        );
    }

    public void update(
            final String sql,
            final Object... params
    ) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (final SQLException ignored) {}

        execute(
                connection,
                PreparedStatement::executeUpdate,
                sql,
                params
        );
    }

    public <T> T queryForObject(
            final Connection connection,
            final String sql,
            final QueryResultMapper<T> mapper,
            final Object... params
    ) {
        return execute(
                connection,
                (pstmt) -> {
                    return mapQueryResult((resultSet -> {
                        if (resultSet.next()) {
                            return mapper.map(resultSet);
                        }
                        throw new IllegalStateException("Query Result Not Found");
                    }), pstmt);
                },
                sql,
                params
        );
    }

    public <T> List<T> queryForList(
            final Connection connection,
            final String sql,
            final QueryResultMapper<T> mapper,
            final Object... params
    ) {
        return execute(
                connection,
                (pstmt) -> {
                    return mapQueryResult((resultSet -> {
                        List<T> results = new ArrayList<>();
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

    private <T> T execute(
            final Connection connection,
            final SqlExecution<T> execution,
            final String sql,
            final Object ... params
    ) {
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(params, pstmt);

            return execution.apply(pstmt);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        } catch (final IllegalStateException e) {
            log.error(e.getMessage(), e);
            throw new InvalidResultSetException(e.getMessage(), e);
        }
    }

    private <T> T mapQueryResult(final QueryResultMapper<T> queryResultMapping, final PreparedStatement pstmt) throws SQLException{
        try (final ResultSet resultSet = pstmt.executeQuery()) {
            return queryResultMapping.map(resultSet);
        }
    }

    private void setParameters(final Object[] params, final PreparedStatement pstmt) throws SQLException {
        if (params.length == 0) {
            return;
        }

        int parameterCount = pstmt.getParameterMetaData().getParameterCount();
        if (params.length != parameterCount) {
            throw new IllegalStateException("PreparedStatement parameter not matches");
        }

        for (int idx = 1; idx <= params.length; idx++) {
            Object param = params[idx - 1];

            pstmt.setObject(idx, param);
        }
    }
}
