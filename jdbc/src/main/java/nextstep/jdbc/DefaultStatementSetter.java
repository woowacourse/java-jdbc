package nextstep.jdbc;

import java.sql.SQLException;
import nextstep.jdbc.element.PreparedStatementSetter;

public class DefaultStatementSetter {

    PreparedStatementSetter getSetter(Object... args) {
        return stmt -> {
            try {
                for (int i = 0; i < args.length; i++) {
                    stmt.setObject(i + 1, args[i]);
                }
            } catch (NullPointerException e) {
                throw new SQLException(e);
            }
        };
    }
}
