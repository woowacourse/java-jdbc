package nextstep.jdbc.utils.preparestatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementSetter {

    public static void psmtSet(PreparedStatement preparedStatement, Object... args)
        throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final Object targetArg = args[i];
            preparedStatement.setObject(i+1, targetArg);
        }
    }
}
