package transaction.stage1.jdbc;

import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        update(sql, createPreparedStatementSetter(parameters));
    }

    public void update(final String sql, final PreparedStatementSetter pss) throws DataAccessException {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            try (final var pstmt = conn.prepareStatement(sql)) {
                pss.setParameters(pstmt);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rm, final Object... parameters) {
        final var list = query(sql, rm, createPreparedStatementSetter(parameters));
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rm, final Object... parameters) {
        return query(sql, rm, createPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rm, final PreparedStatementSetter pss) throws DataAccessException {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            try (final var pstmt = conn.prepareStatement(sql)) {
                pss.setParameters(pstmt);
                try (final var rs = pstmt.executeQuery()) {
                    final var list = new ArrayList<T>();
                    while (rs.next()) {
                        list.add(rm.mapRow(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        };
    }
}
