package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) {

        try (Connection conn = getDataSource().getConnection(); PreparedStatement pstmt = conn
            .prepareStatement(sql)) {

            LOG.debug("query : {}", sql);

            preparedStatementSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    public Object query(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper rowMapper) {

        ResultSet resultSet = null;
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            preparedStatementSetter.setValues(pstmt);

            LOG.debug("query : {}", sql);
            resultSet = executeQuery(pstmt);
            return rowMapper.mapRow(resultSet);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    protected abstract DataSource getDataSource();
}
