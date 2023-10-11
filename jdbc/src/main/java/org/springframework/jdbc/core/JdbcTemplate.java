package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public void execute(String sql, MyPreparedStatementCallback myPreparedStatementCallback) {
        Connection conn = getConnection();
        try (PreparedStatement pstmt = prepareStatement(conn, sql, new Object[0])) {
            myPreparedStatementCallback.execute(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> queryAll(String sql, MyRowMapper<T> rowMapper, Object... params) {
        Connection conn = getConnection();
        try (
            PreparedStatement pstmt = prepareStatement(conn, sql, params);
            ResultSet rs = pstmt.executeQuery()
        ) {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, MyRowMapper<T> rowMapper, Object... params) {
        Connection conn = getConnection();
        try (
            PreparedStatement pstmt = prepareStatement(conn, sql, params);
            ResultSet rs = pstmt.executeQuery();
        ) {
            if (rs.next()) {
                return Optional.of(rowMapper.map(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object[] params)
        throws SQLException {
        log.info("query: {}", sql);

        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 1; i <= params.length; i++) {
            pstmt.setObject(i, params[i - 1]);
        }
        return pstmt;
    }
}
