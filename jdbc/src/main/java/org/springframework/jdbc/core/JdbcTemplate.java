package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, PreparedStatementFunc pstmtFunc, ResultSetMapper<T> rsMapper) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = applyPreparedStatementFunction(conn.prepareStatement(sql), pstmtFunc);
            ResultSet rs = pstmt.executeQuery()
        ) {
            logQuery(sql);
            if (rs.next()) {
                T result = rsMapper.apply(rs);
                if (!rs.next()) {
                    return result;
                }
                throw new IncorrectResultSizeDataAccessException();
            }
            throw new EmptyResultDataAccessException();
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void logQuery(String sql) {
        log.debug("query : {}", sql);
    }

    private void logException(SQLException e) {
        log.error(e.getMessage(), e);
    }

    public <T> List<T> query(String sql, PreparedStatementFunc pstmtFunc, ResultSetMapper<T> rsMapper) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = applyPreparedStatementFunction(conn.prepareStatement(sql), pstmtFunc);
            ResultSet rs = pstmt.executeQuery()
        ) {
            logQuery(sql);
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rsMapper.apply(rs));
            }
            return result;
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(String sql, ResultSetMapper<T> rsMapper) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            logQuery(sql);
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rsMapper.apply(rs));
            }
            return result;
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(String sql, PreparedStatementFunc pstmtFunc) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            logQuery(sql);
            pstmtFunc.apply(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logException(e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement applyPreparedStatementFunction(PreparedStatement ps, PreparedStatementFunc psf)
        throws SQLException {
        psf.apply(ps);
        return ps;
    }

    public interface PreparedStatementFunc {

        void apply(PreparedStatement pst) throws SQLException;
    }
}
