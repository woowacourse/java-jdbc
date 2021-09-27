package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate<T> {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ArgumentPreparedStatementSetter.setValues(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
    }

    public List<T> query(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ArgumentPreparedStatementSetter.setValues(pstmt, args);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
        return results;
    }

    public Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, @Nullable Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ArgumentPreparedStatementSetter.setValues(pstmt, args);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            handleJdbcTemplateException(e);
        }
        return Optional.empty();
    }

    private void handleJdbcTemplateException(final SQLException e) {
        log.error(e.getMessage(), e);
        throw new JdbcTemplateException(e);
    }
}
