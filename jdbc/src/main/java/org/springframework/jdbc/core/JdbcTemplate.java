package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
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

    public void execute(String sql, Object... params) {
        try (var con = dataSource.getConnection();
             var ps = con.prepareStatement(sql)) {
            log.debug("Executing SQL statement [{}]", sql);
            setParams(ps, params);
            ps.execute();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (var con = dataSource.getConnection();
             var ps = con.prepareStatement(sql)) {
            log.debug("Executing SQL statement [{}]", sql);
            setParams(ps, params);
            return result(ps, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = query(sql, rowMapper, params);
        return getSingleResult(results);
    }

    private void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private <T> List<T> result(PreparedStatement ps, RowMapper<T> rowMapper) throws SQLException {
        var rs = ps.executeQuery();
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
}
