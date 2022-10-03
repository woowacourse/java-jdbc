package nextstep.jdbc;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.element.JdbcExecutor;
import nextstep.jdbc.element.PreparedStatementCallBack;
import nextstep.jdbc.element.PreparedStatementCallBackImpl;
import nextstep.jdbc.element.RowMapper;

public class JdbcTemplate {

    private static final PreparedStatementCallBack STATEMENT_CALL_BACK = new PreparedStatementCallBackImpl();

    private final JdbcExecutor jdbcExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.jdbcExecutor = new JdbcExecutor(dataSource);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return jdbcExecutor.executeOrThrow(sql, (stmt) -> {
            final var statement = STATEMENT_CALL_BACK.execute(stmt, sql, args);
            try (final ResultSet rs = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return jdbcExecutor.executeOrThrow(sql, (stmt) -> {
            final var statement = STATEMENT_CALL_BACK.execute(stmt, sql, args);
            try (final ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rowMapper.mapRow(rs);
            }
        });
    }

    public Integer executeUpdate(final String sql, final Object... args) {
        return jdbcExecutor.executeOrThrow(sql, (stmt) -> {
            final var statement = STATEMENT_CALL_BACK.execute(stmt, sql, args);
            return statement.executeUpdate();
        });
    }
}
