package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementTypeValue {

    private final SqlType sqlType;
    private final Object object;

    PreparedStatementTypeValue(final Object object) {
        this.sqlType = SqlType.get(object);
        this.object = object;
    }

    public void setPreparedStatement(final PreparedStatement ps, final int idx) throws SQLException {
        if (sqlType == SqlType.STRING) {
            ps.setString(idx, (String) object);
        }
        if (sqlType == SqlType.BOOLEAN) {
            ps.setBoolean(idx, (boolean) object);
        }
        if (sqlType == SqlType.INT) {
            ps.setInt(idx, (int) object);
        }
        if (sqlType == SqlType.LONG) {
            ps.setLong(idx, (long) object);
        }
    }
}
