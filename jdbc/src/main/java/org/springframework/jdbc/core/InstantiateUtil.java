package org.springframework.jdbc.core;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class InstantiateUtil {

    public static <T> T instantiate(ResultSet rs, Class<T> requiredType, Object[] initArgs)
            throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Class<?>[] columnTypes = new Class[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            int columnType = metaData.getColumnType(i);
            columnTypes[i - 1] = ColumnTypes.convertToClass(columnType);
        }
        Constructor<?> constructor = requiredType.getDeclaredConstructor(columnTypes);
        return requiredType.cast(constructor.newInstance(initArgs));
    }
}
