package com.interface21.jdbc.core.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.interface21.jdbc.core.jpa.annotation.Column;
import com.interface21.jdbc.core.jpa.annotation.Id;
import com.interface21.jdbc.core.jpa.annotation.Table;

public class SqlGenerator {

    public List<Field> getInstanceFields(Class<?> clazz) {
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

    private boolean isColumnField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && (field.isAnnotationPresent(Id.class)
            || field.isAnnotationPresent(Column.class));
    }

    public String generateSelectSql(String tableName, List<Field> fields) {
        StringBuilder columns = new StringBuilder();
        for (Field field : fields) {
            columns.append(getFieldName(field)).append(", ");
        }
        if (!columns.isEmpty()) {
            columns.setLength(columns.length() - 2);
        }
        return "SELECT " + columns + " FROM " + tableName;
    }

    public String generateInsertSql(String tableName, List<Field> fields) {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }
            columns.append(getFieldName(field)).append(", ");
            placeholders.append("?, ");
        }
        
        if (!columns.isEmpty()) {
            columns.setLength(columns.length() - 2);
            placeholders.setLength(placeholders.length() - 2);
        }
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    public String generateUpdateSql(String tableName, List<Field> fields) {
        StringBuilder setClause = new StringBuilder();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }
            setClause.append(getFieldName(field)).append(" = ?, ");
        }
        if (!setClause.isEmpty()) {
            setClause.setLength(setClause.length() - 2);
        }
        return "UPDATE " + tableName + " SET " + setClause;
    }

    public String generateDeleteSql(String tableName) {
        return "DELETE FROM " + tableName;
    }

    public String addWhereClause(String baseSql, String... columnNames) {
        StringBuilder whereClause = new StringBuilder(" WHERE ");
        for (int i = 0; i < columnNames.length; i++) {
            whereClause.append(columnNames[i]).append(" = ?");
            if (i < columnNames.length - 1) {
                whereClause.append(" AND ");
            }
        }
        return baseSql + whereClause.toString();
    }

    public String getTableName(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Entity class must be annotated with @Table");
        }
        Table table = entityClass.getAnnotation(Table.class);
        return table.name();
    }

    private String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.name();
        }
        return field.getName();
    }
}
