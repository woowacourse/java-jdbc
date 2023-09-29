package org.springframework.jdbc.core;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Predicate;

public enum SupportType {
    BOOLEAN(boolean.class, target -> target.getClass().equals(Boolean.class), "setBoolean"),
    BYTE(byte.class, target -> target.getClass().equals(Byte.class), "setByte"),
    SHORT(short.class, target -> target.getClass().equals(Short.class), "setShort"),
    INTEGER(int.class, target -> target.getClass().equals(Integer.class), "setInteger"),
    LONG(long.class, target -> target.getClass().equals(Long.class), "setLong"),
    FLOAT(float.class, target -> target.getClass().equals(Float.class), "setFloat"),
    DOUBLE(double.class, target -> target.getClass().equals(Double.class), "setDoudble"),
    BIG_DECIMAL(BigDecimal.class, target -> target.getClass().equals(BigDecimal.class), "setBigDecimal"),
    STRING(String.class, target -> target.getClass().equals(String.class), "setString"),
    DATE(Date.class, target -> target.getClass().equals(Date.class), "setDate"),
    TIME(Time.class, target -> target.getClass().equals(Time.class), "setTime"),
    TIMESTAMP(Timestamp.class, target -> target.getClass().equals(Timestamp.class), "setTimestamp");

    private final Class<?> classType;
    private final Predicate<Object> checkType;
    private final String preparedStatementMethodName;

    SupportType(final Class<?> classType, final Predicate<Object> checkType, final String preparedStatementMethodName) {
        this.classType = classType;
        this.checkType = checkType;
        this.preparedStatementMethodName = preparedStatementMethodName;
    }

    public static SupportType findType(final Object target) {
        return Arrays.stream(values())
            .filter(type -> type.checkType.test(target))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Not Supported Type. ( type : %s )", target.getClass().getName())
            ));
    }

    public Class<?> getClassType() {
        return classType;
    }

    public String getPreparedStatementMethodName() {
        return preparedStatementMethodName;
    }
}
