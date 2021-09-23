package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public int update(String sql, Object... args) {
        return execute(
            conn -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
                pss.setValues(preparedStatement);
                return preparedStatement;
            }
        );
    }

    private int execute(PreparedStatementStrategy strategy) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = strategy.makePreparedStatement(conn);
            final int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
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
}
