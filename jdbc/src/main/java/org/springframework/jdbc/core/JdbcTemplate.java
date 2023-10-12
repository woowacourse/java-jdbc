package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return execute(connection, sql, pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new RuntimeException();
        }, params);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return execute(connection, sql, pstmt -> {
            ResultSet resultSet = pstmt.executeQuery();
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        }, params);
    }

    private <T> T execute(String sql, PreparedStatementFunction<T> function, Object... params) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = preparedStatementWithParams(connection, sql, params)) {
            return function.execute(pstmt);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Connection conn, String sql, PreparedStatementFunction<T> function, Object... params) {
        try (PreparedStatement pstmt = preparedStatementWithParams(conn, sql, params)) {
            return function.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement preparedStatementWithParams(Connection con, String sql, Object... params)
            throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(sql);
        log.debug("query : {}", sql);
        for (int i = 1; i <= params.length; i++) {
            pstmt.setObject(i, params[i - 1]);
        }
        return pstmt;
    }

    private interface PreparedStatementFunction<T> {
        T execute(PreparedStatement pstmt) throws SQLException;
    }
}
