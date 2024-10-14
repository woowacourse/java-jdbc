package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
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

    public void update(final String sql, final Object... params) {
        process(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return process(sql,
                preparedStatement -> {
                    try (ResultSet resultSet = getResultSet(preparedStatement)) {
                        resultSet.next();
                        if (!resultSet.isLast()) {
                            throw new IllegalStateException("query result set has more than one row");
                        }
                        return rowMapper.mapRow(resultSet, resultSet.getRow());
                    }
                },
                params);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return process(sql,
                preparedStatement -> {
                    try (ResultSet resultSet = getResultSet(preparedStatement)) {
                        List<T> result = new ArrayList<>();
                        while (resultSet.next()) {
                            result.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
                        }
                        return result;
                    }
                },
                params);
    }

    private <T> T process(String sql, PreparedStatementProcessor<T> processor, Object[] params) {
        log.debug("query : {}", sql);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = getPreparedStatement(sql, connection, params)) {
            return processor.process(preparedStatement);
        } catch (SQLException e) {
            throw new JdbcDataAccessException("Failed to process preparedStatement", e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection, Object[] params) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            setPreparedStatement(params, preparedStatement);
            return preparedStatement;
        } catch (SQLException e) {
            throw new CannotGetPreparedStatementException("Failed to get PreparedStatement", e);
        }
    }

    private void setPreparedStatement(Object[] params, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    private ResultSet getResultSet(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeQuery();
    }
}
