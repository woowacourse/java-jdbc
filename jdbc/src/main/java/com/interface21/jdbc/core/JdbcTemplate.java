package com.interface21.jdbc.core;

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
        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(sql)) {
            setParameters(ps, args);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update 실패", e);
        }
    }

    public <T> T queryForObject(String sql, Function<ResultSet, T> rowMapper, Object... args) {
        List<T> query = query(sql, rowMapper, args);
        return query.isEmpty() ? null : query.getLast();
    }

    public <T> List<T> query(String sql, Function<ResultSet, T> rowMapper, Object... args) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = dataSource.getConnection().prepareStatement(sql)) {
            setParameters(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query 실패", e);
        }
        return results;
    }

    private void setParameters(PreparedStatement ps, Object... args) {
        IntStream.range(0, args.length).forEach(i -> setParameterOfIdx(ps, args, i));
    }

    private void setParameterOfIdx(PreparedStatement ps, Object[] args, int parameterIdx) {
        try {
            ps.setObject(parameterIdx+1, args[parameterIdx]);
            log.info("Parameter-{} : {}", parameterIdx+1, args[parameterIdx]);
        } catch (SQLException e) {
            throw new RuntimeException("파라미터 설정 실패", e);
        }
    }
}
