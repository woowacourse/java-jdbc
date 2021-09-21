package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJdbcTemplate implements RowMapper {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcTemplate.class);

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    public void update(PreparedStatementSetter preparedStatementSetter) {
        String sql = createQuery();
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            preparedStatementSetter.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T query() {
        final String sql = createQuery();
        ResultSet rs = null;
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            rs = executeQuery(pstmt);

            return (T) mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    public <T> T queryForObject(PreparedStatementSetter preparedStatementSetter) {
        final String sql = createQuery();
        ResultSet rs = null;
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            preparedStatementSetter.setValues(pstmt);
            rs = executeQuery(pstmt);

            return (T) mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }
}
