package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... objects) {
        return execute(sql, ps -> {
            bindPrepareStatement(ps, objects);
            return ps.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        return execute(sql, ps -> {
            bindPrepareStatement(ps, objects);
            List<T> result = query(sql, rowMapper, objects);
            return getSingleResult(result);
        });
    }

    private <T> T getSingleResult(final List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("데이터가 존재하지 않습니다.");
        }
        if (result.size() > 1) {
            throw new DataAccessException("단일 데이터가 아닙니다.");
        }
        return result.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        return execute(sql, ps -> {
            bindPrepareStatement(ps, objects);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    private void bindPrepareStatement(final PreparedStatement ps, final Object[] objects) throws SQLException {
        int parameterIndex = 1;
        for (Object object : objects) {
            ps.setObject(parameterIndex, object);
            parameterIndex++;
        }
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
