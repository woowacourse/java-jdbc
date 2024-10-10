package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            setParameters(parameters, pstmt);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> result = query(sql, rowMapper, parameters);

        if (result.size() > 1) {
            throw new IncorrectResultSizeDataAccessException();
        }

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.getFirst());
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            setParameters(parameters, pstmt);

            List<T> result = new ArrayList<>();

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            return handleSQLException(e);
        }
    }

    private void setParameters(Object[] parameters, PreparedStatement pstmt) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            pstmt.setObject(i, parameters[i - 1]);
        }
    }

    private <T> T handleSQLException(SQLException e) {
        log.error(e.getMessage(), e);
        throw new DataAccessException(e);
    }
}
