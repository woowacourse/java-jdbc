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

    private PreparedStatement setPrepareStatement(final Object[] args, final PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs;

            setPrepareStatement(args, pstmt);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs;

            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            ArrayList<T> objects = new ArrayList<>();
            while (rs.next()) {
                objects.add(rowMapper.mapRow(rs));
            }
            return objects;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPrepareStatement(args, pstmt);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
