package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    public void update() {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
    }

    private void handleJdbcTemplateException(final SQLException e) {
        log.error(e.getMessage(), e);
        throw new JdbcTemplateException(e);
    }
}
