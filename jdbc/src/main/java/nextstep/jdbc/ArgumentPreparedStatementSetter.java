package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class ArgumentPreparedStatementSetter {

    private ArgumentPreparedStatementSetter() {
    }

    public static void setValues(PreparedStatement ps, Object... args) throws SQLException {
        if (Objects.isNull(args)) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }
}
