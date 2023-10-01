package org.springframework.jdbc.core;

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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... objects) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = preparedStatementAndSetValue(conn, sql, objects)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        try (final Connection conn = dataSource.getConnection();
             final ResultSet rs = preparedStatementAndSetValue(conn, sql, objects).executeQuery()
        ) {
            final List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        try (final Connection conn = dataSource.getConnection();
             final ResultSet rs = preparedStatementAndSetValue(conn, sql, objects).executeQuery()
        ) {
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement preparedStatementAndSetValue(
            final Connection conn,
            final String sql,
            final Object... objects
    ) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        log.debug("query : {}", sql);
        setValues(pstmt, objects);
        return pstmt;
    }

    private void setValues(final PreparedStatement pstmt, Object... objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            pstmt.setObject(i + 1, objects[i]);
        }
    }
}
