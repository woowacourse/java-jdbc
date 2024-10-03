package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate<T> {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final List<T> results = new ArrayList<>();

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            ResultSet resultSet = pstmt.executeQuery();

            mapResults(rowMapper, resultSet);
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("올바르지 않은 쿼리입니다.", e);
        }
    }

    private void mapResults(RowMapper<T> rowMapper, ResultSet resultSet) {
        try {
            while (resultSet.next()) {
                T row = rowMapper.map(resultSet);
                results.add(row);
            }
        } catch (SQLException e) {
            throw new DataAccessException("쿼리 값과 mapper가 맞지 않습니다.", e);
        }

    }

    public int update(String sql, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, parameters);
            log.debug("query : {}", sql);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("올바르지 않은 업데이트 쿼리입니다.", e);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... parameters) {
        try {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        } catch (SQLException e) {
            throw new DataAccessException("쿼리의 파라미터가 올바르지 않습니다.", e);
        }
    }
}
