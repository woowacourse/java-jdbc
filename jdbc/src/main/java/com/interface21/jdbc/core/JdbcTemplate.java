package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, PreparedStatementSetter psSetter) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            psSetter.setValues(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update 실패", e);
        }
    }

    public int update(String sql, Object... args) {
        return update(sql, ps -> setParameters(ps, args));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> query = query(sql, rowMapper, args);
        return query.isEmpty() ? null : query.getLast();
    }

    public <T> List<T> query(String sql, PreparedStatementSetter psSetter, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            psSetter.setValues(ps);
            return retrieveRow(rowMapper, ps);
        } catch (SQLException e) {
            throw new DataAccessException("Query 실패", e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, ps -> setParameters(ps, args), rowMapper);
    }

    private <T> List<T> retrieveRow(RowMapper<T> rowMapper, PreparedStatement ps) throws SQLException {
        List<T> results = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
        }
        return results;
    }

    private void setParameters(PreparedStatement ps, Object... args) {
        IntStream.range(0, args.length).forEach(i -> setParameterOfIdx(ps, args, i));
    }

    private void setParameterOfIdx(PreparedStatement ps, Object[] args, int parameterIdx) {
        try {
            ps.setObject(parameterIdx + 1, args[parameterIdx]);
            log.info("Parameter-{} : {}", parameterIdx + 1, args[parameterIdx]);
        } catch (SQLException e) {
            throw new DataAccessException("파라미터 설정 실패", e);
        }
    }
}
