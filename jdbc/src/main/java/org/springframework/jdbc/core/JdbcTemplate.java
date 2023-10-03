package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) {
        try (var con = dataSource.getConnection();
             var ps = psc.createPreparedStatement(con)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public void execute(String sql, Object... args) {
        execute(new SimplePreparedStatementCreator(sql), ps -> {
            var pss = newArgPreparedStatementSetter(args);
            pss.setValues(ps);
            return ps.execute();
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(new SimplePreparedStatementCreator(sql), ps -> {
            var pss = newArgPreparedStatementSetter(args);
            pss.setValues(ps);
            var rs = ps.executeQuery();
            return result(rs, rowMapper);
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        return getSingleResult(results);
    }

    private <T> List<T> result(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }

    private <T> T getSingleResult(Collection<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("empty result");
        }
        if (results.size() > 1) {
            throw new DataAccessException("not 1 size, size is " + results.size());
        }
        return results.iterator().next();
    }

    private PreparedStatementSetter newArgPreparedStatementSetter(Object[] args) {
        return new ArgumentPreparedStatementSetter(args);
    }
}
