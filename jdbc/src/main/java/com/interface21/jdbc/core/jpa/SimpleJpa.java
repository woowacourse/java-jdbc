package com.interface21.jdbc.core.jpa;

import java.lang.reflect.Field;
import java.util.List;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.core.jpa.annotation.Id;

public class SimpleJpa {

    private final JdbcTemplate jdbcTemplate;
    private final JpaCache cache;

    public SimpleJpa(JdbcTemplate jdbcTemplate, String basePackage) {
        this.jdbcTemplate = jdbcTemplate;
        this.cache = new JpaCache(basePackage);
    }

    public <T> List<T> selectAll(final Class<T> entityClass) {
        String sql = cache.getSelectSql(entityClass);
        RowMapper<T> rowMapper = cache.getRowMapper(entityClass);
        return jdbcTemplate.query(sql, rowMapper);
    }

    public <T> List<T> selectById(final Class<T> entityClass, Object id) {
        String sql = cache.getSelectByIdSql(entityClass);
        RowMapper<T> rowMapper = cache.getRowMapper(entityClass);
        return jdbcTemplate.query(sql, rowMapper, id);
    }

    public <T> List<T> selectByColumn(final Class<T> entityClass, String columnName, Object value) {
        String sql = cache.getSelectByColumnsSql(entityClass, columnName);
        RowMapper<T> rowMapper = cache.getRowMapper(entityClass);
        return jdbcTemplate.query(sql, rowMapper, value);
    }

    public <T> List<T> selectByColumns(final Class<T> entityClass, String[] columnNames, Object... values) {
        String sql = cache.getSelectByColumnsSql(entityClass, columnNames);
        RowMapper<T> rowMapper = cache.getRowMapper(entityClass);
        return jdbcTemplate.query(sql, rowMapper, values);
    }

    public void insert(final Object entity) {
        String sql = cache.getInsertSql(entity.getClass());
        Object[] params = extractEntityParameters(entity, true);
        jdbcTemplate.execute(sql, params);
    }

    public <T> void update(T entity) {
        String sql = cache.getUpdateByIdSql(entity.getClass());
        Object[] entityParams = extractEntityParameters(entity, true);
        Object idValue = extractIdValue(entity);
        Object[] allParams = combineArrays(entityParams, new Object[] {idValue});
        jdbcTemplate.execute(sql, allParams);
    }

    public void deleteById(final Class<?> entityClass, Object id) {
        String sql = cache.getDeleteByIdSql(entityClass);
        jdbcTemplate.execute(sql, id);
    }

    private Object[] extractEntityParameters(Object entity, boolean skipAutoId) {
        List<Object> params = new java.util.ArrayList<>();
        try {
            List<Field> fields = cache.getFields(entity.getClass());
            for (Field field : fields) {
                Object value = field.get(entity);
                if (skipAutoId && field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                params.add(value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return params.toArray();
    }

    private Object extractIdValue(Object entity) {
        try {
            List<Field> fields = cache.getFields(entity.getClass());
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field.get(entity);
                }
            }
            throw new IllegalArgumentException("No @Id field found in entity: " + entity.getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] combineArrays(Object[] first, Object[] second) {
        Object[] result = new Object[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
