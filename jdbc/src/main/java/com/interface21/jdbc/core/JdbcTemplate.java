package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            setQueryParameter(pstmt, args);
            log.debug("query : {}", sql);
            return executeQuery(pstmt, rowMapper);

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> executeQuery(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> queryResult = new ArrayList<>();
            while (rs.next()) {
                queryResult.add(rowMapper.mapRow(rs));
            }
            return queryResult;
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        List<T> queryResult = query(sql, rowMapper, args);
        if (queryResult.size() > 1) {
            throw new DataAccessException("단건 데이터 조회에서 다중 데이터가 조회되었습니다");
        }
        if (queryResult.isEmpty()) {
            return null;
        }
        return queryResult.getFirst();
    }

    public int update(final String sql, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            setQueryParameter(pstmt, args);
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public int update(final String sql, GeneratedKeyHolder keyHolder, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ) {

            setQueryParameter(pstmt, args);

            log.debug("query : {}", sql);
            int affectedRows = pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    keyHolder.setKey(rs.getObject(1));
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setQueryParameter(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
