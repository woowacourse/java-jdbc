package nextstep.jdbc;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.element.JdbcExecutor;
import nextstep.jdbc.element.RowMapper;
import nextstep.jdbc.element.PreparedStatementCallBack;
import nextstep.jdbc.element.PreparedStatementCallBackImpl;

public class JdbcTemplate {

    private static final PreparedStatementCallBack STATEMENT_CALL_BACK = new PreparedStatementCallBackImpl();

    private final JdbcExecutor jdbcExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.jdbcExecutor = new JdbcExecutor(dataSource);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return jdbcExecutor.executeOrThrow(sql, (stmt) -> {
            final var statement = STATEMENT_CALL_BACK.execute(stmt, sql, args);
            try (final ResultSet rs = statement.executeQuery()) {
                List<T> result = new LinkedList<>();
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return jdbcExecutor.executeOrThrow(sql, (stmt) -> {
            final var statement = STATEMENT_CALL_BACK.execute(stmt, sql, args);
            try (final ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rowMapper.mapRow(rs);
            }
        });
    }

    public Integer executeUpdate(String sql, Object... args) {
        return jdbcExecutor.executeOrThrow(sql, (stmt) -> {
            final var statement = STATEMENT_CALL_BACK.execute(stmt, sql, args);
            return statement.executeUpdate();
        });
    }
}
