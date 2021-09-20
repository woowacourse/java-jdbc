package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            new ArgumentPreparedStatementSetter(args).setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            new ArgumentPreparedStatementSetter(args).setValues(pstmt);
            return rowMapper.mapRow(pstmt.executeQuery());
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
