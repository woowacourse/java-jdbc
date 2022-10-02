package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void executeUpdate(String sql, PreparedStatementStrategy strategy) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            strategy.makePreparedStatement(pstmt);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Object queryForObject(String sql, ResultSetStrategyForObject strategy) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            strategy.setParameters(pstmt);

            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            return strategy.mapRows(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public List<Object> queryForList(String sql, ResultSetStrategyForList strategy) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            return strategy.mapRows(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
