package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(final User user) {
        final var sql = createQuery();

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setValues(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(User user, PreparedStatement pstmt) throws SQLException;
}
