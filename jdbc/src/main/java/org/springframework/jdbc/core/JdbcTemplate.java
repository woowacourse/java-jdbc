package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataUpdateException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
            throw new DataUpdateException(e.getMessage(), e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, arguments);
            log.debug("query : {}", sql);
            return executePreparedStatement(ps -> {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Optional<T> result = Optional.ofNullable(rowMapper.getRow(rs));
                    if (!rs.last()){
                        throw new DataAccessException();
                    }
                    return result;
                }
                throw new NoSuchElementException();
            }, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... arguments) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, arguments);
            log.debug("query : {}", sql);
            return executePreparedStatement(ps -> {
                ResultSet rs = pstmt.executeQuery();
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.getRow(rs));
                }
                return results;
            }, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setArguments(PreparedStatement pstmt, Object[] arguments) throws SQLException {
        PreparedStatementSetter psSetter = getPreparedStatementSetter(arguments);
        psSetter.setValues(pstmt);
    }

    private PreparedStatementSetter getPreparedStatementSetter(Object[] arguments) {
        return pstmt -> {
            for (int i = 1; i < arguments.length + 1; i++) {
                try {
                    pstmt.setObject(i, arguments[i - 1]);
                } catch (SQLException e) {
                    throw new DataAccessException(e.getMessage());
                }
            }
        };
    }

    private <T> T executePreparedStatement(PreparedStatementExecutor psExecutor, PreparedStatement pstmt) {
        try {
            return (T) psExecutor.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
