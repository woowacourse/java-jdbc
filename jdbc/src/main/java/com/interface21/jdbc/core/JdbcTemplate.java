package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.NoSingleResultException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql) {
        return execute(sql, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(sql, ps -> extractResults(rowMapper, ps.executeQuery()));
    }

    public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        return execute(sql, ps -> extractResults(rowMapper, ps.executeQuery()), pss);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, ps -> extractResults(rowMapper, ps.executeQuery()), args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        List<T> results = query(sql, rowMapper);
        if (results.size() == 1) {
            return results.get(0);
        }
        throw new NoSingleResultException("조회 결과가 하나가 아닙니다. size: " + results.size());
    }

    public <T> T queryForObject(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) {
        List<T> results = query(sql, pss, rowMapper);
        if (results.size() == 1) {
            return results.get(0);
        }
        throw new NoSingleResultException("조회 결과가 하나가 아닙니다. size: " + results.size());
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        if (results.size() == 1) {
            return results.get(0);
        }
        throw new NoSingleResultException("조회 결과가 하나가 아닙니다. size: " + results.size());
    }

    public int update(String sql, PreparedStatementSetter pss) {
        return execute(sql, PreparedStatement::executeUpdate, pss);
    }

    public int update(String sql, Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback,
                          PreparedStatementSetter pss) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            pss.setValues(ps);
            return callback.doInPreparedStatement(ps);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback, Object... args) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = createPreparedStatement(connection, sql, args)) {
            return callback.doInPreparedStatement(ps);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection connection, String sql, Object... args)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        setValues(args.clone(), ps);
        return ps;
    }

    private void setValues(Object[] objects, PreparedStatement ps) throws SQLException {
        for (int i = 1; i <= objects.length; i++) {
            ps.setObject(i, objects[i - 1]);
        }
    }

    private <T> List<T> extractResults(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapToObject(rs));
        }
        return results;
    }
}
