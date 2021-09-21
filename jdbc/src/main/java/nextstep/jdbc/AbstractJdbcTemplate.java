package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJdbcTemplate {

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

    public <T> List<T> query(RowMapper<T> rowMapper) {
        final String sql = createQuery();
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = executeQuery(pstmt)) {

            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.mapRow(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) {
        final String sql = createQuery();
        ResultSet rs = null;
        try (Connection conn = getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            preparedStatementSetter.setValues(pstmt);
            rs = executeQuery(pstmt);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            throw new IllegalArgumentException();
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
