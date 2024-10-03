package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.mapper.Mapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public <T> T query(Class<T> clazz, String sql, Object... params) {
        List<T> result = queryForAll(clazz, sql, params);

        if (result.isEmpty()) {
            return null;
        }
        if (result.size() != 1) {
            throw new DataAccessException("다수의 데이터가 조회되었습니다.");
        }

        return result.getFirst();
    }

    public <T> List<T> queryForAll(Class<T> clazz, String sql, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = getStatement(conn, sql, params);
                ResultSet rs = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);
            return Mapper.queryResolver(clazz, sql, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(String sql, Object... params) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = getStatement(conn, sql, params);
        ) {
            log.debug("query : {}", sql);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getStatement(Connection conn, String sql, Object[] params) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }

        return pstmt;
    }
}
