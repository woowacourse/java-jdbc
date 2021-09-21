package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJdbcTemplate implements PreparedStatementSetter, RowMapper {

    private static final Logger log = LoggerFactory.getLogger(AbstractJdbcTemplate.class);

    protected abstract String createQuery();

    protected abstract DataSource getDataSource();

    public void update() {
        String sql = createQuery();
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setValues(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Object query() {
        final String sql = createQuery();
        ResultSet rs = null;
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            setValues(pstmt);
            rs = executeQuery(pstmt);

            return mapRow(rs);
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

    @Override
    public void setValues(PreparedStatement pstmt) throws SQLException {

    }
}
