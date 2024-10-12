package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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
    private static final int SINGLE_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(Connection conn, String sql, Object... params){
        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
            PreparedStatementSetter pss = createPreparedStatementSetter(params);
            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void update(String sql, Object... params){
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            PreparedStatementSetter pss = createPreparedStatementSetter(params);
            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection(){
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new CannotGetJdbcConnectionException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> result = query(sql, rowMapper, params);
        if (result.isEmpty() || result.size() != SINGLE_SIZE) {
            throw new DataAccessException("Unexpected number of rows returned: " + result.size());
        }
        return result.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            PreparedStatementSetter pss = createPreparedStatementSetter(params);
            pss.setValues(pstmt);
            ResultSet rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            return createListResultFromResultSet(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... params) {
        return (pstmt) -> {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + SINGLE_SIZE, params[i]);
            }
        };
    }

    private <T> List<T> createListResultFromResultSet(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }
}
