package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... values) {
        preparedStatementExecutor.execute(
                getPreparedStatementGenerator(sql, values),
                PreparedStatement::executeUpdate
        );
    }

    public <T> Optional<T> queryForObject(final String sql, final Mapper<T> mapper, final Object... values) {
        final List<T> result = preparedStatementExecutor.execute(
                getPreparedStatementGenerator(sql, values),
                getMultiplePreparedStatementCaller(mapper)
        );
        if (result.isEmpty()) {
            return Optional.empty();
        } else if (result.size() > 1) {
            throw new IllegalArgumentException("Too many results");
        }
        return Optional.of(result.get(0));
    }

    public <T> List<T> query(final String sql, final Mapper<T> mapper, final Object... values) {
        return preparedStatementExecutor.execute(
                getPreparedStatementGenerator(sql, values),
                getMultiplePreparedStatementCaller(mapper)
        );
    }

    private <T> PreparedStatementCaller<List<T>> getMultiplePreparedStatementCaller(final Mapper<T> mapper) {
        return psmt -> {
            final ResultSet rs = psmt.executeQuery();
            final List<T> result = new ArrayList<>();

            while (rs.next()) {
                result.add(mapper.map(rs));
            }

            return result;
        };
    }

    private PreparedStatementGenerator getPreparedStatementGenerator(final String sql, final Object[] values) {
        return conn -> {
            final PreparedStatement psmt = conn.prepareStatement(sql);
            setValues(psmt, values);

            return psmt;
        };
    }

    private void setValues(final PreparedStatement psmt, final Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            psmt.setObject(i + 1, values[i]);
        }
    }
}
