package nextstep.jdbc.jdbcparam;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public enum JdbcParamType {
    INTEGER(Integer.class, (statement, index, param) -> statement.setInt(index, (Integer) param) ),
    LONG(Long.class, (statement, index, param) -> statement.setLong(index, (Long) param)),
    STRING(String.class, (statement, index, param) -> statement.setString(index, (String) param)),
    ;

    private final Class<?> clazz;
    private final JdbcParamSetter paramSetter;

    JdbcParamType(final Class<?> clazz, final JdbcParamSetter paramSetter) {
        this.clazz = clazz;
        this.paramSetter = paramSetter;
    }

    public static void setParam(final PreparedStatement statement, final int index, final Object param)
            throws SQLException {
        final JdbcParamType foundType = Arrays.stream(values())
                .filter(type -> type.clazz.isInstance(param))
                .findFirst()
                .orElseThrow();
        foundType.paramSetter.setParam(statement, index, param);
    }
}
