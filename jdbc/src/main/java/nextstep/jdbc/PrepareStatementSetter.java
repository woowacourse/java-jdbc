package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrepareStatementSetter {

    public static void setValue(PreparedStatement preparedStatement, Object... querySubject)
        throws SQLException {
        for (int i = 0; i < querySubject.length; i++) {
            preparedStatement.setObject(i + 1, querySubject[i]);
        }
    }
}
