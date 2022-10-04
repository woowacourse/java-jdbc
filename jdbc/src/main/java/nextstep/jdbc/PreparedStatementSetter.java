package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreparedStatementSetter {

    public void setParams(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (var index = 0; index < args.length; index++) {
            preparedStatement.setObject(index + 1, args[index]);
        }
    }
}
