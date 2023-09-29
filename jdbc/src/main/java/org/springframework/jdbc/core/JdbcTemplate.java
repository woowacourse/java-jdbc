package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = makePreparedWhenHasParams(conn, sql, params)) {

            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> mapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = makePreparedWhenHasParams(conn, sql, params);
             ResultSet rs = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            while (rs.next()) {
                return mapper.get(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement makePreparedWhenHasParams(final Connection conn, final String sql, final Object[] params)
            throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }

        return pstmt;
    }

    public <T> List<T> queryForObjects(final String sql, final RowMapper<T> mapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            log.debug("query : {}", sql);

            final var selectQueryResult = new ArrayList<T>();
            while (rs.next()) {
                selectQueryResult.add(mapper.get(rs));
            }
            return selectQueryResult;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
