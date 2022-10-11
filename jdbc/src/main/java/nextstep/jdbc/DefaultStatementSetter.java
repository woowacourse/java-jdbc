package nextstep.jdbc;

import java.sql.SQLException;
import nextstep.jdbc.element.PreparedStatementSetter;

public class DefaultStatementSetter {

    public DefaultStatementSetter() {
    }

    PreparedStatementSetter getSetter(Object... args) {
        return stmt -> {
            try {
                for (int i = 0; i < args.length; i++) {
                    // null 이나 잘못된 값 처리는 jdbcExecutor 에서 진행
                    stmt.setObject(i + 1, args[i]);
                }
            } catch (NullPointerException e) {
                throw new SQLException(e);
            }
        };
    }
}
