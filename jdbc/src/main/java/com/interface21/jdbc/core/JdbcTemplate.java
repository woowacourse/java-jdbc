package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, PreparedStatementSetter pss) {
        execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public void update(String sql, Object... args) {
        update(sql, createArgumentPreparedStatementSetter(args));
    }

    public <T> List<T> query(String sql, RowMapper<T> rm, PreparedStatementSetter pss) {
        return execute(sql, pss, ps -> mapResultSet(rm, ps));
    }

    public <T> List<T> query(String sql, RowMapper<T> rm, Object... args) {
        return query(sql, rm, createArgumentPreparedStatementSetter(args));
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rm, PreparedStatementSetter pss) {
        List<T> results = query(sql, rm, pss);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new SQLExecuteException("조회된 레코드가 2건 이상입니다.");
        }
        return Optional.ofNullable(results.getFirst());
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rm, Object... args) {
        return queryForObject(sql, rm, createArgumentPreparedStatementSetter(args));
    }

    private PreparedStatementSetter createArgumentPreparedStatementSetter(Object[] args) {
        return ps -> {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
        };
    }

    private <T> List<T> mapResultSet(RowMapper<T> rm, PreparedStatement ps) {
        try (ResultSet rs = ps.executeQuery()) {
            List<T> entityList = new ArrayList<>();
            while (rs.next()) {
                T entity = rm.mapRow(rs, rs.getRow());
                entityList.add(entity);
            }
            return entityList;
        } catch (SQLException e) {
            throw new SQLExecuteException("SQL을 실행할 수 없습니다.", e);
        }
    }

    private <T> T execute(String sql, PreparedStatementSetter pss, SqlExecutor<T> sqlExecutor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pss.setValues(ps);
            return sqlExecutor.execute(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new SQLExecuteException("SQL을 실행할 수 없습니다.", e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
