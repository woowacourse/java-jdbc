package org.springframework.jdbc.core;

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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        try (
                final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            ResultSet rs = null;
            try {
                setParameters(pstmt, args);
                final List<T> objects = new ArrayList<>();
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    objects.add(rowMapper.mapRow(rs));
                }
                return objects;
            } finally {
                closeResultSetResource(rs);
            }
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            ResultSet rs = null;
            try {
                setParameters(pstmt, args);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(rowMapper.mapRow(rs));
                }
                return Optional.empty();
            } finally {
                closeResultSetResource(rs);
            }
        });
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private void closeResultSetResource(@Nullable final ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException ignored) {
        }
    }
}
