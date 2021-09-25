package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MultiParameterResolver {

    public void resolve(PreparedStatement preparedStatement, int index, Object data) throws SQLException {
        preparedStatement.setObject(index, data);
    }
}
