package com.interface21.jdbc.core;

import com.interface21.jdbc.result.SelectMultiResult;
import com.interface21.jdbc.result.SelectSingleResult;
import com.interface21.jdbc.util.WrapperToPrimitiveConverter;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void write(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            pstmt.executeUpdate();
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public SelectSingleResult selectOne(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            final ResultSet rs = pstmt.executeQuery();
            return parseSelectSingle(rs);
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public SelectMultiResult selectMulti(final String sql, final Object... params) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setStatementsWithPOJOType(pstmt, params);
            final ResultSet rs = pstmt.executeQuery();
            return parseSelectMulti(rs);
        } catch (final SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void setStatementsWithPOJOType(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            setStatementWithPOJOType(pstmt, i, params[i]);
        }
    }

    private void setStatementWithPOJOType(final PreparedStatement pstmt, final int index, final Object param) {
        try {
            final Method method = findMethodWithPOJO(param);
            method.invoke(pstmt, index + 1, param);
        } catch (final InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethodWithPOJO(final Object param) {
        final Class<?> paramClass = param.getClass();
        final String typeName = paramClass.getSimpleName();
        try {
            return PreparedStatement.class.getMethod("set" + typeName, int.class, WrapperToPrimitiveConverter.getPrimitiveClass(paramClass));
        } catch (final NoSuchMethodException e) {
            try {
                return PreparedStatement.class.getMethod("setObject", int.class, Object.class);
            } catch (final NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private SelectSingleResult parseSelectSingle(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final Map<String, Object> map = new HashMap<>();
        if (resultSet.next()) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
        }
        return new SelectSingleResult(map);
    }

    private SelectMultiResult parseSelectMulti(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final Map<String, List<Object>> resultMap = initialize(metaData);
        while (resultSet.next()) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                final String columnName = metaData.getColumnName(i);
                final Object columnValue = resultSet.getObject(i);

                resultMap.get(columnName)
                        .add(columnValue);
            }
        }
        final int last = resultSet.getRow();
        return new SelectMultiResult(resultMap, last);
    }

    private Map<String, List<Object>> initialize(final ResultSetMetaData metaData) throws SQLException {
        final Map<String, List<Object>> resultMap = new HashMap<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            resultMap.put(metaData.getColumnName(i), new ArrayList<>());
        }
        return resultMap;
    }
}
