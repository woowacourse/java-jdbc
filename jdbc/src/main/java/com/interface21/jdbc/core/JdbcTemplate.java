package com.interface21.jdbc.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> select(final String tableName, final Class<T> entityClass,
        final Map<String, Object> conditions) {
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
                    Field[] fields = entityClass.getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setAccessible(true);
                        fields[i].set(entity, rs.getObject(i + 1));
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

    public void insert(final String tableName, final Object entity) {
        final var sql = generateInsertSql(tableName, entity);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Field[] fields = entity.getClass().getDeclaredFields();
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

    public <T> void update(String tableName, T entity, Map<String, Object> conditions) {
        String condition = generateCondition(conditions);
        String sql = generateUpdateSql(tableName, entity, condition.isEmpty() ? null : condition);

        try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Field[] fields = entity.getClass().getDeclaredFields();
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

    public void delete(String tableName, Map<String, Object> conditions) {
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

    private String generateSelectSql(String tableName, Class<?> entityClass, String condition) {
        Field[] fields = entityClass.getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        for (Field field : fields) {
            columns.append(field.getName()).append(", ");
        }
        if (!columns.isEmpty()) {
            columns.setLength(columns.length() - 2);
        }
        return "SELECT " + columns + " FROM " + tableName + (condition != null ? " WHERE " + condition : "");
    }

    private String generateInsertSql(String tableName, Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (field.getName().equals("id") && (value == null || value.equals(0L))) {
                    continue;
                }
                columns.append(field.getName()).append(", ");
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
        Field[] fields = entity.getClass().getDeclaredFields();
        StringBuilder setClause = new StringBuilder();
        for (Field field : fields) {
            setClause.append(field.getName()).append(" = ?, ");
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
