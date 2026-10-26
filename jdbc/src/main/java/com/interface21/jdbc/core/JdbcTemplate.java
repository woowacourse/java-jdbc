package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected abstract String createQuery();

    public abstract DataSource getDataSource();

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    public void update() {
        String sql = createQuery();

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
