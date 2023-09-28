package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, PreparedStatementFunc pstmtFunc, ResultSetMapper<T> rsFunc) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = applyPreparedStatementFunction(conn.prepareStatement(sql), pstmtFunc);
            ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            if (rs.next()) {
                T result = rsFunc.apply(rs);
                if (!rs.next()) {
                    return result;
                }
            }
            throw new DataAccessException("No data is available");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(String sql, PreparedStatementFunc pstmtFunc) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmtFunc.apply(pstmt);
            pstmt.executeUpdate();
            log.debug("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
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

    public interface ResultSetMapper<T> {

        T apply(ResultSet rs) throws SQLException;
    }
}
