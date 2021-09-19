package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class JdbcTemplate implements RowMapper, PreparedStatementSetter {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();


    public void update() {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
    }

    public <T> T query() {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValues(pstmt);
            return mapRow(executeQuery(pstmt));
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
        return null;
    }

    private ResultSet executeQuery(PreparedStatement pstmt) {
        try {
            return pstmt.executeQuery();
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
        return null;
    }

    private void handleJdbcTemplateException(final SQLException e) {
        log.error(e.getMessage(), e);
        throw new JdbcTemplateException(e);
    }
}
