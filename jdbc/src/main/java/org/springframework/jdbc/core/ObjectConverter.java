package org.springframework.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectConverter {

    private ObjectConverter() {
    }

    public static <T> T convertForObject(ResultSet resultSet, Class<T> type){
        try {
            Constructor<T> allFieldsConstructor = findAllFieldsConstructor(type);
            if(resultSet.next()){
                return toInstance(allFieldsConstructor, resultSet);
            }
            throw new EmptyResultSetException();
        } catch (
            InvocationTargetException |
            InstantiationException |
            SQLException |
            IllegalAccessException e) {
            throw new ResultSetConvertException("ResultSet 을 변환하는데 실패했습니다.", e);
        }
    }

    public static <T> List<T> convertForList(ResultSet resultSet, Class<T> type) {
        try {
            List<T> result = new ArrayList<>();
            Constructor<T> allFieldsConstructor = findAllFieldsConstructor(type);
            while(resultSet.next()){
                result.add(toInstance(allFieldsConstructor, resultSet));
            }
            return result;
        } catch (
            InvocationTargetException |
            InstantiationException |
            SQLException |
            IllegalAccessException e) {
            throw new ResultSetConvertException("ResultSet 을 변환하는데 실패했습니다.", e);
        }
    }

    private static <T> Constructor<T> findAllFieldsConstructor(Class<T> type) {
        List<String> fieldNames = extractFieldNames(type);
        Constructor<?> noAllFieldInitializingConstructor = Stream.of(type.getDeclaredConstructors())
            .filter(constructor -> haveAllField(constructor, fieldNames))
            .findAny()
            .orElseThrow(() -> new ResultSetConvertException("no All Field Initializing Constructor"));
        noAllFieldInitializingConstructor.setAccessible(true);
        return (Constructor<T>) noAllFieldInitializingConstructor;
    }

    private static <T> List<String> extractFieldNames(Class<T> type) {
        return Stream.of(type.getDeclaredFields())
            .map(Field::getName)
            .collect(Collectors.toList());
    }

    private static boolean haveAllField(Constructor<?> constructor, List<String> fieldNames) {
        Parameter[] allParameters = constructor.getParameters();
        if (fieldNames.size() != allParameters.length) {
            return false;
        }
        return Stream.of(allParameters)
            .allMatch(parameter -> fieldNames.contains(parameter.getName()));
    }

    private static <T> T toInstance(Constructor<T> constructor, ResultSet resultSet)
        throws SQLException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Parameter[] parameters = constructor.getParameters();
        int length = parameters.length;
        Object[] args = new Object[length];
        List<String> resultColumnFields = getResultColumnFields(resultSet);
        for (int i = 0; i < length; i++) {
            args[i] = extractParam(parameters[i], resultSet, resultColumnFields);
        }
        return constructor.newInstance(args);
    }

    private static List<String> getResultColumnFields(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<String> resultColumnFields = new ArrayList<>();
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            resultColumnFields.add(metaData.getColumnName(i+1));
        }
        return resultColumnFields;
    }

    private static Object extractParam(Parameter parameter, ResultSet resultSet, List<String> resultColumnFields)
        throws SQLException {
        String parameterName = parameter.getName();
        if (resultColumnFields.contains(parameterName.toUpperCase())) {
            return resultSet.getObject(parameterName.toUpperCase());
        }
        return null;
    }
}
