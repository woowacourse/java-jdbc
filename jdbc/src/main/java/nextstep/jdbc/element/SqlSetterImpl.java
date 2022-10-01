package nextstep.jdbc.element;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import nextstep.jdbc.element.SqlSetter;

public class SqlSetterImpl implements  SqlSetter {
    @Override
    public PreparedStatement execute(PreparedStatement stmt, String sql, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            stmt.setObject(i + 1, args[i]);
        }
        return stmt;
    }
}
