package nextstep.jdbc;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.element.JdbcExecutor;
import nextstep.jdbc.element.ResultSetCallback;
import nextstep.jdbc.element.RowMapper;

public class JdbcTemplate {

    private final JdbcExecutor jdbcExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.jdbcExecutor = new JdbcExecutor(dataSource);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final ResultSetCallback<List<T>> resultSetCallback = rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        };
        return jdbcExecutor.find(sql, resultSetCallback, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final ResultSetCallback<T> resultSetCallback = rs -> {
            rs.next();
            return rowMapper.mapRow(rs);
        };
        return jdbcExecutor.find(sql, resultSetCallback, args);
    }

    public Integer executeUpdate(final String sql, final Object... args) {
        return jdbcExecutor.update(sql, args);
    }
}
