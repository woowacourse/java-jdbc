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

public class JdbcTemplate {

    protected static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... parameters) throws SQLException {
        update(sql, createPreparedStatementSetter(parameters));
    }

    public void update(String sql, PreparedStatementSetter preparedStatementSetter) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        try (conn; pstmt) {

            preparedStatementSetter.setValues(pstmt);
            pstmt.executeUpdate();
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... parameters) {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        };
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        preparedStatementSetter.setValues(pstmt);
        ResultSet rs = pstmt.executeQuery();

        try (conn; pstmt; rs) {
            log.debug("query : {}", sql);

            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        }
    }

    public <T> List<T> queryForList(String sql, PreparedStatementSetter preparedStatementSetter, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();

        Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        preparedStatementSetter.setValues(pstmt);
        ResultSet rs = pstmt.executeQuery();

        try (conn; pstmt; rs) {
            log.debug("query : {}", sql);

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }

}
