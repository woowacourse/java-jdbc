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
    private static final int SINGLE_ROW_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter pstmtSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info("query : {}", sql);

            pstmtSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter pstmtSetter, RowMapper<T> rowMapper) {
        List<T> rows = query(sql, pstmtSetter, rowMapper);
        return extractSingleRow(rows);
    }

    private <T> T extractSingleRow(List<T> rows) {
        validateRowsSize(rows);
        return rows.getFirst();
    }

    private <T> void validateRowsSize(List<T> rows) {
        if (rows.isEmpty()) {
            throw new EmptyResultDataAccessException("반환할 수 있는 결과가 존재하지 않습니다.");
        }
        if (rows.size() > SINGLE_ROW_SIZE) {
            throw new IncorrectResultSizeDataAccessException("하나의 결과를 반환할 것을 기대했지만, 반환할 수 있는 결과가 많습니다.");
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return query(sql, pstmt -> {}, rowMapper);
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pstmtSetter, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info("query : {}", sql);

            pstmtSetter.setValues(pstmt);
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
}
