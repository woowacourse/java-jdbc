package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, args);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, Function<ResultSet, T> rowMapper, Object... args) {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return results;
    }

    public <T> T queryForObject(String sql, Function<ResultSet, T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.apply(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setParameters(PreparedStatement ps, Object... args) throws SQLException {
        IntStream.range(0, args.length).forEach(parameterIndex -> {
            try {
                ps.setObject(parameterIndex + 1, args[parameterIndex]);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        });
    }
}
