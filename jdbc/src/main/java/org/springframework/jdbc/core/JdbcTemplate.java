package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setObject(i, params[i - 1]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            log.debug("query : {}", sql);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setObject(i, params[i - 1]);
            }
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            throw new RuntimeException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
