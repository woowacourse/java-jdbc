package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... values) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {
            setValues(psmt, values);
            psmt.execute();
            log.info("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void setValues(PreparedStatement psmt, Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            psmt.setObject(i + 1, values[i]);
        }
    }
}
