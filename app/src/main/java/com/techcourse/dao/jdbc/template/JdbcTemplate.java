package com.techcourse.dao.jdbc.template;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(User user) {
        final String sql = createQuery();

        final DataSource dataSource = this.getDataSource();
        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            setValues(user, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(User user, PreparedStatement pstmt) throws SQLException;
}
