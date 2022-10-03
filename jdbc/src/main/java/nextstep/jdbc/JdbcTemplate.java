package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, @Nullable final Object... args) {
        try (final Connection conn = dataSource.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            setArguments(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        Assert.notNull(sql, "SQL must not be null");

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            return mapResultSetToList(rowMapper, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            setArguments(pstmt, args);
            ResultSet rs = pstmt.executeQuery();
            return mapResultSetToObject(rowMapper, rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private <T> T mapResultSetToObject(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> ts = mapResultSetToList(rowMapper, rs);
        if (ts.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        return ts.iterator().next();
    }

    private <T> List<T> mapResultSetToList(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        final List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return result;
    }

    private void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        if (args == null) {
            return;
        }

        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }
}
