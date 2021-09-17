package com.techcourse.dao.jdbc.template;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract DataSource getDataSource();

    public Object query(String sql, PreparedStatementSetter pstmtSetter, RowMapper rowMapper) {

        final DataSource dataSource = this.getDataSource();
        ResultSet rs = null;
        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmtSetter.setValues(pstmt);
            rs = executeQuery(pstmt);
            LOG.debug("query : {}", sql);
            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public void update(String sql, PreparedStatementSetter pstmtSetter) {

        final DataSource dataSource = this.getDataSource();
        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmtSetter.setValues(pstmt);
            LOG.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
