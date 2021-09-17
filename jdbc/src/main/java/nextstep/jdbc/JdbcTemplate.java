package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private PreparedStatementSetter pstmtSetter;
    private RowMapper rowMapper;

    public JdbcTemplate(PreparedStatementSetter pstmtSetter, RowMapper rowMapper) {
        this.pstmtSetter = pstmtSetter;
        this.rowMapper = rowMapper;
    }

    public JdbcTemplate(PreparedStatementSetter pstmtSetter) {
        this.pstmtSetter = pstmtSetter;
    }

    public JdbcTemplate(RowMapper rowMapper) {
        this.rowMapper = rowMapper;
    }

    public void update() {
        DataSource dataSource = getDataSource();
        final String sql = createQuery();

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            if (Objects.nonNull(pstmtSetter)) {
                pstmtSetter.setValues(pstmt);
            }

            pstmt.executeUpdate();

            log.debug("query : {}", sql);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public Object query() {
        DataSource dataSource = getDataSource();
        final String sql = createQuery();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            if (Objects.nonNull(pstmtSetter)) {
                pstmtSetter.setValues(pstmt);
            }

            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException ignored) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();
}
