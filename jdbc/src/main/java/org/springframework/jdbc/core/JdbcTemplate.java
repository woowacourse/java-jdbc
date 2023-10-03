package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... values) {
        preparedStatementExecutor.execute(
                conn -> {
                    final PreparedStatement psmt = conn.prepareStatement(sql);
                    setValues(psmt, values);
                    return psmt;
                },
                PreparedStatement::executeUpdate
        );
    }

    public <T> T queryForObject(final String sql, final Mapper<T> mapper, final Object... values) {
        return preparedStatementExecutor.execute(
                conn -> {
                    final PreparedStatement psmt = conn.prepareStatement(sql);
                    setValues(psmt, values);
                    return psmt;
                },
                psmt -> {
                    final ResultSet rs = psmt.executeQuery();
                    if (rs.next()) {
                        return mapper.map(rs);
                    }
                    return null;
                }
        );
    }

    public <T> List<T> query(final String sql, final Mapper<T> rowMapper, final Object... values) {
        return preparedStatementExecutor.execute(
                conn -> {
                    final PreparedStatement psmt = conn.prepareStatement(sql);
                    setValues(psmt, values);
                    return psmt;
                },
                psmt -> {
                    final ResultSet rs = psmt.executeQuery();
                    final List<T> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(rowMapper.map(rs));
                    }
                    return result;
                }
        );
    }

    private void setValues(final PreparedStatement psmt, final Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            psmt.setObject(i + 1, values[i]);
        }
    }
}
