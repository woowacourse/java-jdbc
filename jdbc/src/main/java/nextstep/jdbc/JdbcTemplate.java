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
            return pstmt.executeUpdate();
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

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, args, new RowMapperResultSetExtractor<>(rowMapper, 1));
        // todo, dataaccessutils
        return results.get(0);
    }

    private <T> List<T> query(String sql, Object[] args, RowMapperResultSetExtractor<T> rse) {
        return query(sql, new ArgumentPreparedStatementSetter(args), rse);
    }

    private <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapperResultSetExtractor<T> rse) {
        return query(
            conn -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                pss.setValues(preparedStatement);
                return preparedStatement;
            },
            rse
        );
    }

    private <T> List<T> query(PreparedStatementStrategy strategy, RowMapperResultSetExtractor<T> rse) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            pstmt = strategy.makePreparedStatement(conn);
            rs = pstmt.executeQuery();
            return rse.extractData(rs);
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
}
