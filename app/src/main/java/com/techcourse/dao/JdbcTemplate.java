package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    protected JdbcTemplate() {
    }

    protected abstract String createQuery();

    public void update() {
        String sql = createQuery();

        try (Connection conn = getDataSource().getConnection(); PreparedStatement pstmt = conn
            .prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract DataSource getDataSource();

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;
}
