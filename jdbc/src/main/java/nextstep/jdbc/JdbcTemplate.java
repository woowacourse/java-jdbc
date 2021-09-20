package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.exception.SqlQueryException;
import nextstep.exception.SqlUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... objects) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            PreparedStatementSetter.setValues(pstmt, objects);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlUpdateException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    public Object query(String sql, RowMapper rowMapper, Object... objects) {

        ResultSet resultSet = null;
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            PreparedStatementSetter.setValues(pstmt, objects);

            LOG.debug("query : {}", sql);
            resultSet = executeQuery(pstmt);
            return rowMapper.mapRow(resultSet);
        } catch (SQLException e) {
            throw new SqlQueryException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
