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

public class JdbcTemplate<T> {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final List<T> results = new ArrayList<>();

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public T executeQueryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> results = executeQuery(sql, rowMapper, parameters);
        if (results.isEmpty()) {
            log.info("{} : 조회 결과가 없습니다.", sql);
            return null;
        }
        return results.getFirst();
    }

    public List<T> executeQuery(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, parameters);
            log.debug("query : {}", sql);
            ResultSet resultSet = pstmt.executeQuery();

            mapResults(rowMapper, resultSet);
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void mapResults(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            T row = rowMapper.map(resultSet);
            results.add(row);
        }
    }

    public int updateQuery(String sql, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, parameters);
            log.debug("query : {}", sql);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }
}
