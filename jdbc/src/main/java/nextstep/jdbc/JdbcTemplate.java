package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.UpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update() {
        String sql = createQuery();

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getDataSource().getConnection();
            pstmt = conn.prepareStatement(sql);

            LOG.debug("query: {}", sql);

            setValues(pstmt);
            pstmt.executeUpdate();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);

            throw new UpdateException();
        } finally {
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

    public abstract String createQuery();

    public abstract DataSource getDataSource();

    public abstract void setValues(PreparedStatement pstmt) throws SQLException;
}
