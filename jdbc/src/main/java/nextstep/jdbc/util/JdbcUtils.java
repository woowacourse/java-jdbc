package nextstep.jdbc.util;

import nextstep.jdbc.exception.UnSupportedTypeException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcUtils {
    private JdbcUtils() {
    }

    public static Object getSingleResultSetValue(ResultSet rs, Class<?> columnType) throws SQLException {
        if (String.class == columnType) {
            return rs.getString(1);
        }
        else if (boolean.class == columnType || Boolean.class == columnType) {
            return rs.getBoolean(1);
        }
        else if (byte.class == columnType || Byte.class == columnType) {
            return rs.getByte(1);
        }
        else if (short.class == columnType || Short.class == columnType) {
            return rs.getShort(1);
        }
        else if (int.class == columnType || Integer.class == columnType) {
            return rs.getInt(1);
        }
        else if (long.class == columnType || Long.class == columnType) {
            return rs.getLong(1);
        }
        else if (float.class == columnType || Float.class == columnType) {
            return rs.getFloat(1);
        }
        else if (double.class == columnType || Double.class == columnType ||
                Number.class == columnType) {
            return rs.getDouble(1);
        }
        throw new UnSupportedTypeException("지원하지 않는 타입입니다.");
    }
}