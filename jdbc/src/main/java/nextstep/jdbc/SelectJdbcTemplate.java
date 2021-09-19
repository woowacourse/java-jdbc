package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SelectJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    protected abstract String createQuery();

    private ResultSet executeQuery(PreparedStatement pstmt) {
        try {
            return pstmt.executeQuery();
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
        return null;
    }

    protected abstract DataSource getDataSource();

    protected abstract Object mapRow(ResultSet rs) throws SQLException;

    public Object query() {
        try (Connection conn = getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(createQuery())) {
            setValues(pstmt);
            return mapRow(executeQuery(pstmt));
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
        return null;
    }

    protected abstract void setValues(PreparedStatement pstmt) throws SQLException;

    private void handleJdbcTemplateException(final SQLException e) {
        log.error(e.getMessage(), e);
        throw new JdbcTemplateException(e);
    }
}
