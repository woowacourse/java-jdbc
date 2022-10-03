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

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... params) {
        final PreparedStatementCreator creator = preparedStatementCreator(params);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = creator.create(conn, sql)
        ) {
            log.debug("query : {}", sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final PreparedStatementCreator creator = preparedStatementCreator(params);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = creator.create(conn, sql);
             ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            return mapToList(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> mapToList(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.map(rs));
        }
        return result;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final PreparedStatementCreator creator = preparedStatementCreator(params);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = creator.create(conn, sql);
             ResultSet rs = pstmt.executeQuery()
        ) {
            log.debug("query : {}", sql);
            return mapToObject(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T mapToObject(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        return rs.next() ? rowMapper.map(rs) : null;
    }

    private PreparedStatementCreator preparedStatementCreator(final Object... params) {
        return (conn, sql) -> {
            final PreparedStatement pstmt = conn.prepareStatement(sql);
            int index = 1;
            for (Object param : params) {
                pstmt.setObject(index++, param);
            }
            return pstmt;
        };
    }
}
