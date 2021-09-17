package nextstep.jdbc;

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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... objects) {

        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < objects.length; i++) {
                setPstmt(pstmt, i + 1, objects[i]);
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... objects) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < objects.length; i++) {
                setPstmt(pstmt, i + 1, objects[i]);
            }

            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }

            throw new SQLException();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        List<T> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(
                sql); ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            while (rs.next()) {
                users.add(rowMapper.mapRow(rs));
            }

            return users;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPstmt(PreparedStatement pstmt, int i, Object object) throws SQLException {
        if (object instanceof Long) {
            pstmt.setLong(i, (Long) object);
            return;
        }
        pstmt.setString(i, (String) object);
    }
}
