package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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
    private static final int VALID_RESULT_COUNT = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... obj) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setSqlParameter(obj, pstmt);
            pstmt.execute();
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    private void setSqlParameter(final Object[] obj, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < obj.length; i++) {
            pstmt.setObject(i + 1, obj[i]);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... obj) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = getPreparedStatement(sql, obj, conn);
             final ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);
            final List<T> result = convertResultSetToInstances(rowMapper, rs);
            validateResultSetSize(result);
            return Optional.of(result.get(0));
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }

    private PreparedStatement getPreparedStatement(final String sql, final Object[] obj, final Connection conn) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        setSqlParameter(obj, pstmt);
        return pstmt;
    }

    private <T> void validateResultSetSize(List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("조회하려는 레코드가 존재하지 않습니다.");
        }
        if (result.size() > VALID_RESULT_COUNT) {
            throw new DataAccessException("조회하려는 레코드는 2개 이상일 수 없습니다.");
        }
    }

    private <T> List<T> convertResultSetToInstances(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return result;
    }
    
    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... obj) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = getPreparedStatement(sql, obj, conn);
             final ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);
            return convertResultSetToInstances(rowMapper, rs);
        } catch (SQLException exception) {
            log.error(exception.getMessage(), exception);
            throw new DataAccessException(exception);
        }
    }
}
