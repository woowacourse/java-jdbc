package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        execute(new PreparedStatementCreator(sql), ps -> {
            final var pss = newArgumentPreparedStatementSetter(parameters);
            pss.setValues(ps);
            return ps.executeUpdate();
        });
    }

    public <T> T execute(PreparedStatementCreator psc, StatementCallback<T> action) {
        try (var conn = dataSource.getConnection();
             var stmt = psc.createPreparedStatement(conn)) {
            return action.doInStatement(stmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private ArgumentPreparedStatementSetter newArgumentPreparedStatementSetter(final Object... parameters) {
        return new ArgumentPreparedStatementSetter(parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> result = query(sql, rowMapper, parameters);
        if (result.size() != 1) {
            throw new IllegalStateException("Query 조회 결과가 하나가 아닙니다.");
        }
        return result.iterator().next();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(new PreparedStatementCreator(sql), ps -> {
            final var pss = new ArgumentPreparedStatementSetter(parameters);
            pss.setValues(ps);
            final var rs = ps.executeQuery();
            return result(rowMapper, rs);
        });
    }

    private <T> List<T> result(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }
}
