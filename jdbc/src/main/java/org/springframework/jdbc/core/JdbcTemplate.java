package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.exception.DataAccessException;
import org.springframework.jdbc.exception.DataUpdateException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... arguments) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setArguments(pstmt, arguments);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataUpdateException(e.getMessage());
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, arguments);
            log.debug("query : {}", sql);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.getRow(rs, rs.getRow());
                }
            }
            throw new NoSuchElementException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, arguments);
            log.debug("query : {}", sql);
            List<T> results = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.getRow(rs, rs.getRow()));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private void setArguments(PreparedStatement pstmt, Object[] arguments) throws SQLException {
        PreparedStatementSetter psSetter = getPreparedStatementSetter(arguments);
        psSetter.setValues(pstmt);
    }

    private PreparedStatementSetter getPreparedStatementSetter(Object[] arguments) {
        PreparedStatementSetter psSetter = ps -> {
            for (int i = 1; i < arguments.length + 1; i++) {
                try {
                    ps.setObject(i, arguments[i - 1]);
                } catch (SQLException e) {
                    throw new DataAccessException(e.getMessage());
                }
            }
        };
        return psSetter;
    }
}
