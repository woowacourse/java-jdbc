package nextstep.jdbc;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.element.JdbcExecutor;
import nextstep.jdbc.element.PreparedStatementSetter;
import nextstep.jdbc.element.ResultSetCallback;
import nextstep.jdbc.element.RowMapper;

public class JdbcTemplate {

    private final JdbcExecutor jdbcExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.jdbcExecutor = new JdbcExecutor(dataSource);
    }


    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final Object... args) {
        return query(sql, getStatementSetter(args), rowMapper);
    }

    public <T> List<T> query(final String sql,
                             final PreparedStatementSetter statementSetter,
                             final RowMapper<T> rowMapper) {
        final ResultSetCallback<List<T>> resultSetCallback = rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        };
        return jdbcExecutor.find(sql, statementSetter, resultSetCallback);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final Object... args) {
        return queryForObject(sql, getStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(final String sql,
                                final PreparedStatementSetter statementSetter,
                                final RowMapper<T> rowMapper) {
        final ResultSetCallback<T> resultSetCallback = rs -> {
            rs.next();
            return rowMapper.mapRow(rs);
        };
        return jdbcExecutor.find(sql, statementSetter, resultSetCallback);
    }

    public Integer executeUpdate(final String sql, final Object... args) {
        return executeUpdate(sql, getStatementSetter(args));
    }

    public Integer executeUpdate(final String sql,
                                 final PreparedStatementSetter statementSetter) {
        return jdbcExecutor.update(sql, statementSetter);
    }

    private PreparedStatementSetter getStatementSetter(Object[] args) {
        return stmt -> {
            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
        };
    }
}
