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
    private Connection currentConnection;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setCurrentConnection() {
        try {
            this.currentConnection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCurrentConnection(final Connection connection) {
        if (connection == null) {
            setCurrentConnection();
            return;
        }
        this.currentConnection = connection;
    }

    public void update(final String sql, final Object... parameters) {
        executeSql(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T query(final String sql, final ResultSetMapper<T> resultSetMapper, final Object... parameters) {
        QueryExecutor<T> queryExecutor = (pstmt) -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetMapper.map(rs);
                }
                return null;
            }
        };

        return executeSql(sql, queryExecutor, parameters);
    }

    public <T> List<T> queryMany(final String sql, final ResultSetMapper<T> resultSetMapper, final Object... parameters) {
        QueryExecutor<List<T>> queryExecutor = (pstmt) -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetMapper.map(rs));
                }
                return result;
            }
        };

        return executeSql(sql, queryExecutor, parameters);
    }

    private <T> T executeSql(final String sql, final QueryExecutor<T> queryExecutor, final Object... parameters) {
        try (PreparedStatement pstmt = currentConnection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, parameters);
            return queryExecutor.run(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }
}
