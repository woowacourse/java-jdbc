package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementSetter {

    private StatementSetter() {
        throw new RuntimeException("생성할 수 없는 클래스입니다.");
    }

    public static void setValues(final PreparedStatement statement, final Object... objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            statement.setObject(i + 1, objects[i]);
        }
    }
}
