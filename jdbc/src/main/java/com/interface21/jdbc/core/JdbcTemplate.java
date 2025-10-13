package com.interface21.jdbc.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.annotation.Column;
import com.interface21.jdbc.core.annotation.Id;
import com.interface21.jdbc.core.annotation.Table;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> select(final Class<T> entityClass, final Map<String, Object> conditions) {
        String tableName = getTableName(entityClass);
        final var condition = generateCondition(conditions);
        final var sql = generateSelectSql(tableName, entityClass, condition.isEmpty() ? null : condition);

        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Object value : conditions.values()) {
                pstmt.setObject(paramIndex++, value);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    T entity = entityClass.getDeclaredConstructor().newInstance();
                    List<Field> fields = getInstanceFields(entityClass);
                    for (int i = 0; i < fields.size(); i++) {
                        fields.get(i).setAccessible(true);
                        fields.get(i).set(entity, rs.getObject(i + 1));
                    }
                    results.add(entity);
                }
            }
            return results;
        } catch (SQLException | IllegalAccessException | NoSuchMethodException | InstantiationException |
                 InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private boolean isColumnField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && (field.isAnnotationPresent(Id.class)
            || field.isAnnotationPresent(Column.class));
    }

    public void insert(final Object entity) {
        String tableName = getTableName(entity.getClass());
        final var sql = generateInsertSql(tableName, entity);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            List<Field> fields = getInstanceFields(entity.getClass());
            int paramIndex = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (field.getName().equals("id") && (value == null || value.equals(0L))) {
                    continue;
                }
                pstmt.setObject(paramIndex++, value);
            }
            pstmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> void update(T entity, Map<String, Object> conditions) {
        String tableName = getTableName(entity.getClass());
        String condition = generateCondition(conditions);
        String sql = generateUpdateSql(tableName, entity, condition.isEmpty() ? null : condition);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            List<Field> fields = getInstanceFields(entity.getClass());
            int paramIndex = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                pstmt.setObject(paramIndex++, value);
            }
            for (Object value : conditions.values()) {
                pstmt.setObject(paramIndex++, value);
            }
            pstmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void delete(final Class<?> entityClass, Map<String, Object> conditions) {
        String tableName = getTableName(entityClass);
        String condition = generateCondition(conditions);
        String sql = generateDeleteSql(tableName, condition.isEmpty() ? null : condition);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            for (Object value : conditions.values()) {
                pstmt.setObject(paramIndex++, value);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> String getTableName(Class<T> entityClass) {
        if (!entityClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Entity class must be annotated with @Table");
        }
        Table table = entityClass.getAnnotation(Table.class);
        return table.name();
    }

    private List<Field> getInstanceFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (!fields.contains(field) && isColumnField(field)) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private String generateSelectSql(String tableName, Class<?> entityClass, String condition) {
        List<Field> fields = getInstanceFields(entityClass);
        StringBuilder columns = new StringBuilder();
        for (Field field : fields) {
            columns.append(getFieldName(field)).append(", ");
        }
        if (!columns.isEmpty()) {
            columns.setLength(columns.length() - 2);
        }
        return "SELECT " + columns + " FROM " + tableName + (condition != null ? " WHERE " + condition : "");
    }

    private String generateInsertSql(String tableName, Object entity) {
        List<Field> fields = getInstanceFields(entity.getClass());
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (field.isAnnotationPresent(Id.class) && (value == null || value.equals(0L))) {
                    continue;
                }
                columns.append(getFieldName(field)).append(", ");
                placeholders.append("?, ");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 마지막 쉼표와 공백 제거
        if (!columns.isEmpty()) {
            columns.setLength(columns.length() - 2);
            placeholders.setLength(placeholders.length() - 2);
        }
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    private String generateUpdateSql(String tableName, Object entity, String condition) {
        List<Field> fields = getInstanceFields(entity.getClass());
        StringBuilder setClause = new StringBuilder();
        for (Field field : fields) {
            setClause.append(getFieldName(field)).append(" = ?, ");
        }
        // 마지막 쉼표와 공백 제거
        if (!setClause.isEmpty()) {
            setClause.setLength(setClause.length() - 2);
        }
        return "UPDATE " + tableName + " SET " + setClause + (condition != null ? " WHERE " + condition : "");
    }

    private String generateDeleteSql(String tableName, String condition) {
        return "DELETE FROM " + tableName + (condition != null ? " WHERE " + condition : "");
    }

    private String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.name();
        }
        return field.getName();
    }

    private String generateCondition(Map<String, Object> conditions) {
        StringBuilder condition = new StringBuilder();
        for (String key : conditions.keySet()) {
            condition.append(key).append(" = ? AND ");
        }
        if (!condition.isEmpty()) {
            condition.setLength(condition.length() - 5);
        }
        return condition.toString();
    }
}
