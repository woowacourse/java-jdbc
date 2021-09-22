package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SelectJdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(SelectJdbcTemplate.class);

    public Object query() {
        String sql = createQuery();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);
            setValues(pstmt);
            rs = executeQuery(pstmt);

            LOG.debug("query: {}", sql);

            return mapRow(rs);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new QueryException();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignored) {
            }

            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) {
        try {
            return pstmt.executeQuery();
        } catch (Exception e) {
            throw new QueryException();
        }
    }

    public abstract String createQuery();

    public abstract DataSource getDataSource();

    public abstract void setValues(PreparedStatement pstmt) throws SQLException;

    public abstract Object mapRow(ResultSet rs) throws SQLException;
}
