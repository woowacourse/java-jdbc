package nextstep.jdbc;

import java.util.Arrays;

public enum SqlParameterValue {

    LONG(Long.class, (pstmt, i, o) -> pstmt.setLong(i, (Long) o)),
    STRING(String.class, (pstmt, i, o) -> pstmt.setString(i, (String) o));

    private final Class<?> clazz;
    private final PreparedStatementSetter pstmts;

    SqlParameterValue(Class<?> clazz, PreparedStatementSetter pstmts) {
        this.clazz = clazz;
        this.pstmts = pstmts;
    }

    public static PreparedStatementSetter findSetter(Class<?> clazz) {
        return Arrays.stream(SqlParameterValue.values())
                .filter(x -> x.clazz.equals(clazz))
                .map(x -> x.pstmts)
                .findFirst()
                .orElseThrow(() -> new JdbcException("Cannot find sql parameter type"));
    }
}
