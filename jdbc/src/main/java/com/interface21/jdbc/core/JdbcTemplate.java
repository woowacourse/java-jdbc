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

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final int PARAMETER_INDEX_OFFSET = 1;
    private static final int EXPECTED_COUNT = 1;
    private static final String SQL_EXCEPTION_MESSAGE = "쿼리를 실행하던 도중 예외가 발생했습니다. sql = %s";
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, parameters);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(String.format(SQL_EXCEPTION_MESSAGE, sql), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, parameters);
            return findResults(pstmt, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(String.format(SQL_EXCEPTION_MESSAGE, sql), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, parameters);
            return getOnlyOneResult(pstmt, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(String.format(SQL_EXCEPTION_MESSAGE, sql), e);
        }
    }

    private <T> T getOnlyOneResult(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = findResults(pstmt, rowMapper);
        if (results.isEmpty()) {
            throw new DataAccessException("일치하는 데이터가 없습니다.");
        }
        if (results.size() > EXPECTED_COUNT) {
            throw new DataAccessException("일치하는 데이터가 2개 이상입니다.");
        }
        return results.getFirst();
    }

    private <T> List<T> findResults(PreparedStatement pstmt, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }

    private void setParameters(PreparedStatement pstmt, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + PARAMETER_INDEX_OFFSET, parameters[i]);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
