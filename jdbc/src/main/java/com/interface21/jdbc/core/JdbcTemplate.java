package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public int executeUpdate(String sql, Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            log.debug("query : {}", sql);
            setQueryParameter(pstmt, values);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            setQueryParameter(pstmt, values);
            ResultSet rs = pstmt.executeQuery();
            return collectResultSet(rs, rowMapper, sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> collectResultSet(ResultSet resultSet, RowMapper<T> rowMapper, String sql) throws SQLException {
        List<T> resultSets = new ArrayList<>();
        log.debug("query : {}", sql);
        while (resultSet.next()) {
            resultSets.add(rowMapper.mapRow(resultSet));
        }
        return resultSets;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        List<T> resultSets = query(sql, rowMapper, values);
        return resultSets.getFirst();
    }

    private void setQueryParameter(PreparedStatement statement, Object... values) throws SQLException {
        for(int i = 0; i < values.length; i++) {
            statement.setObject(i + 1, values[i]);
        }
    }
}
