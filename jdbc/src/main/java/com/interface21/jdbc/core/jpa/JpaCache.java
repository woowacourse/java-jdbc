package com.interface21.jdbc.core.jpa;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import com.interface21.jdbc.core.RowMapper;
import com.interface21.jdbc.core.jpa.annotation.Table;

class JpaCache {

    private final SqlGenerator sqlGenerator;
    private final Map<Class<?>, RowMapper<?>> rowMapperCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Field>> fieldsCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, String> selectSqlCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, String> selectByIdSqlCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, String> insertSqlCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, String> updateByIdSqlCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, String> deleteByIdSqlCache = new ConcurrentHashMap<>();

    public JpaCache(String basePackage) {
        this.sqlGenerator = new SqlGenerator();
        initializeCache(basePackage);
    }

    private void initializeCache(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Table.class);
        
        for (Class<?> entityClass : entityClasses) {
            cacheEntityMetadata(entityClass);
        }
    }

    private void cacheEntityMetadata(Class<?> entityClass) {
        // 1. 필드 목록 캐싱
        List<Field> fields = sqlGenerator.getInstanceFields(entityClass);
        for (Field field : fields) {
            field.setAccessible(true);
        }
        fieldsCache.put(entityClass, fields);

        // 2. RowMapper 캐싱
        RowMapper<?> rowMapper = createRowMapperForEntity(entityClass, fields);
        rowMapperCache.put(entityClass, rowMapper);

        // 3. SQL 캐싱
        cacheSqlStatements(entityClass);
    }

    private RowMapper<?> createRowMapperForEntity(Class<?> entityClass, List<Field> fields) {
        return rs -> {
            try {
                Object entity = entityClass.getDeclaredConstructor().newInstance();
                for (int i = 0; i < fields.size(); i++) {
                    fields.get(i).setAccessible(true);
                    fields.get(i).set(entity, rs.getObject(i + 1));
                }
                return entity;
            } catch (Exception e) {
                throw new RuntimeException("Failed to map row to entity: " + entityClass.getName(), e);
            }
        };
    }

    private void cacheSqlStatements(Class<?> entityClass) {
        String tableName = sqlGenerator.getTableName(entityClass);
        
        // SELECT SQL들 캐싱
        String selectSql = sqlGenerator.generateSelectSql(tableName, entityClass);
        String selectByIdSql = sqlGenerator.addWhereClause(selectSql, "id");
        selectSqlCache.put(entityClass, selectSql);
        selectByIdSqlCache.put(entityClass, selectByIdSql);
        
        // INSERT/UPDATE/DELETE SQL들 캐싱
        String insertSql = sqlGenerator.generateInsertSql(tableName, entityClass);
        String updateSql = sqlGenerator.generateUpdateSql(tableName, entityClass);
        String updateByIdSql = sqlGenerator.addWhereClause(updateSql, "id");
        String deleteSql = sqlGenerator.generateDeleteSql(tableName);
        String deleteByIdSql = sqlGenerator.addWhereClause(deleteSql, "id");
        
        insertSqlCache.put(entityClass, insertSql);
        updateByIdSqlCache.put(entityClass, updateByIdSql);
        deleteByIdSqlCache.put(entityClass, deleteByIdSql);
    }

    @SuppressWarnings("unchecked")
    public <T> RowMapper<T> getRowMapper(Class<T> entityClass) {
        return (RowMapper<T>) rowMapperCache.get(entityClass);
    }

    public List<Field> getFields(Class<?> entityClass) {
        return fieldsCache.get(entityClass);
    }

    public String getSelectSql(Class<?> entityClass) {
        return selectSqlCache.get(entityClass);
    }

    public String getSelectByIdSql(Class<?> entityClass) {
        return selectByIdSqlCache.get(entityClass);
    }

    public String getSelectByColumnsSql(Class<?> entityClass, String... columnNames) {
        String baseSql = selectSqlCache.get(entityClass);
        return sqlGenerator.addWhereClause(baseSql, columnNames);
    }

    public String getInsertSql(Class<?> entityClass) {
        return insertSqlCache.get(entityClass);
    }

    public String getUpdateByIdSql(Class<?> entityClass) {
        return updateByIdSqlCache.get(entityClass);
    }

    public String getDeleteByIdSql(Class<?> entityClass) {
        return deleteByIdSqlCache.get(entityClass);
    }

    public boolean isCached(Class<?> entityClass) {
        return fieldsCache.containsKey(entityClass);
    }
}
