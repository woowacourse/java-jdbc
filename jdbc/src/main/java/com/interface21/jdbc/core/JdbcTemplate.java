package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.conversion.TypeConversionService;
import com.interface21.jdbc.core.querybuilder.delete.DeleteQueryBuilder;
import com.interface21.jdbc.core.querybuilder.delete.DeleteWhereStep;
import com.interface21.jdbc.core.querybuilder.insert.InsertQueryBuilder;
import com.interface21.jdbc.core.querybuilder.insert.InsertValueStep;
import com.interface21.jdbc.core.querybuilder.select.SelectColumnsStep;
import com.interface21.jdbc.core.querybuilder.select.SelectQueryBuilder;
import com.interface21.jdbc.core.querybuilder.select.SelectSqlStep;
import com.interface21.jdbc.core.querybuilder.select.SelectStringQueryBuilder;
import com.interface21.jdbc.core.querybuilder.update.UpdateQueryBuilder;
import com.interface21.jdbc.core.querybuilder.update.UpdateSetStep;
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
    private final TypeConversionService typeConversionService;

    public JdbcTemplate(final DataSource dataSource, final TypeConversionService typeConversionService) {
        this.dataSource = dataSource;
        this.typeConversionService = typeConversionService;
    }

    public void update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL update failed", e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        log.debug("query : {}", sql);

        List<T> results = query(sql, rowMapper, params);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("Expected 1 result, but got " + results.size());
        }
        return Optional.of(results.get(0));
    }

    public <T> Optional<T> queryForObject(String sql, Class<T> clazz, Object... params) {
        return queryForObject(sql, new ColumnMatchingRowMapper<>(clazz, typeConversionService), params);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("SQL query failed", e);
        }
    }

    public <T> List<T> query(String sql, Class<T> clazz, Object... params) {
        return query(sql, new ColumnMatchingRowMapper<>(clazz, typeConversionService), params);
    }

    public <T> SelectSqlStep<T> selectWithSql(Class<T> clazz) {
        return new SelectStringQueryBuilder<>(this, clazz);
    }

    public <T> SelectColumnsStep<T> select(Class<T> clazz) {
        return new SelectQueryBuilder<>(this, clazz);
    }

    public InsertValueStep insertInto(String tableName) {
        return new InsertQueryBuilder(this, tableName);
    }

    public UpdateSetStep update(String tableName) {
        return new UpdateQueryBuilder(this, tableName);
    }

    public DeleteWhereStep deleteFrom(String tableName) {
        return new DeleteQueryBuilder(this, tableName);
    }
}
