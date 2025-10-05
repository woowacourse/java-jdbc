package com.interface21.jdbc.core;

import com.interface21.jdbc.DataAccessException;
import com.interface21.jdbc.IncorrectResultSizeException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void update(String sql, Object... params){
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            log.debug("query : {}", sql);
            setParameters(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params){
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            log.debug("query : {}", sql);
            setParameters(pstmt, params);
            return executionResult(rowMapper, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) throws SQLException {
        List<T> results = query(sql, rowMapper, params);
        if(results.isEmpty()){
            throw new IncorrectResultSizeException("No result found for query.");
        }
        if(results.size() > 1){
            throw new IncorrectResultSizeException("Returns more than 1 row.");
        }
        return results.getFirst();
    }

    private void setParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for(int i=0; i<parameters.length; i++){
            pstmt.setObject(i+1, parameters[i]);
        }
    }

    private <T> List<T> executionResult(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        try(ResultSet resultSet = pstmt.executeQuery()) {
            return mapResults(rowMapper, resultSet);
        }
    }

    private <T> List<T> mapResults(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNumber = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs,rowNumber++));
        }
        return results;
    }
}
