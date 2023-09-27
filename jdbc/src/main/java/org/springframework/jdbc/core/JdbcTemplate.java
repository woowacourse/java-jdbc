package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... values) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement psmt = conn.prepareStatement(sql)) {
            setValues(psmt, values);
            psmt.execute();
            log.info("query : {}", sql);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T executeQuery(final String sql, final Mapper<T> mapper, final Object... values) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement psmt = conn.prepareStatement(sql)) {
            setValues(psmt, values);
            final ResultSet rs = psmt.executeQuery();
            log.info("query : {}", sql);
            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setValues(final PreparedStatement psmt, final Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            psmt.setObject(i + 1, values[i]);
        }
    }
}
