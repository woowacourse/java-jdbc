package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.exception.EmptyResultDataAccessException;
import com.interface21.jdbc.exception.IncorrectResultSizeDataAccessException;
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

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info("query : {}", sql);

            setArguments(args, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setArguments(Object[] args, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info("query : {}", sql);

            setArguments(args, pstmt);
            return extractMultiRows(rowMapper, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> extractMultiRows(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            return getRows(rowMapper, rs);
        }
    }

    private <T> List<T> getRows(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> list = new ArrayList<>();

        while (rs.next()) {
            list.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return list;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info("query : {}", sql);

            setArguments(args, pstmt);
            return extractSingleRow(rowMapper, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T extractSingleRow(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            return getRow(rowMapper, rs);
        }
    }

    private <T> T getRow(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        if (rs.next()) {
            T row = rowMapper.mapRow(rs, rs.getRow());
            validateIncorrectResultSize(rs);
            return row;
        }
        throw new EmptyResultDataAccessException("반환할 수 있는 결과가 존재하지 않습니다.");
    }

    private void validateIncorrectResultSize(ResultSet rs) throws SQLException {
        if (rs.next()) {
            throw new IncorrectResultSizeDataAccessException("하나의 결과를 반환할 것을 기대했지만, 반환할 수 있는 결과가 많습니다.");
        }
    }
}
