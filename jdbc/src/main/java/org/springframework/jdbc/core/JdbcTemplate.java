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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int PARAMETER_INDEX_OFFSET = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... parameters) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = createPrepareStatement(sql, conn, parameters)
        ) {
            pstmt.executeUpdate();
            log.debug("query : {}", sql);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = createPrepareStatement(sql, conn, parameters);
            ResultSet rs = pstmt.executeQuery()
        ) {
            List<T> results = new ArrayList<>();
            log.debug("query : {}", sql);

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = createPrepareStatement(sql, conn, parameters);
            ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatement createPrepareStatement(String sql, Connection conn, Object... parameters)
        throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        bindParameters(pstmt, parameters);
        return pstmt;
    }

    private void bindParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + PARAMETER_INDEX_OFFSET, parameters[i]);
        }
    }
}
