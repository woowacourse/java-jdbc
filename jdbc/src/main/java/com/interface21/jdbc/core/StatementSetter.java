package com.interface21.jdbc.core;

import com.interface21.jdbc.util.WrapperToPrimitiveConverter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementSetter {

    private static final Class<PreparedStatement> statementClass = PreparedStatement.class;
    private static final int START_INDEX = 1;
    private static final Method setObjectMethod;

    static {
        try {
            setObjectMethod = statementClass.getMethod("setObject", int.class, Object.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setStatementsWithPOJOType(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            setStatementWithPOJOType(pstmt, i, params[i]);
        }
    }

    private static void setStatementWithPOJOType(final PreparedStatement pstmt, final int index, final Object param) {
        try {
            final Method method = findMethodWithPOJO(param);
            method.invoke(pstmt, index + START_INDEX, param);
        } catch (final InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Method findMethodWithPOJO(final Object param) {
        final Class<?> paramClass = param.getClass();
        final String typeName = paramClass.getSimpleName();
        try {
            return statementClass.getMethod("set" + typeName, int.class, WrapperToPrimitiveConverter.getPrimitiveClass(paramClass));
        } catch (final NoSuchMethodException e) {
            return setObjectMethod;
        }
    }

    private StatementSetter() {}
}
