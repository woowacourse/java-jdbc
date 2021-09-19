package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringMultiParameterResolver implements MultiParameterResolver<String> {

    @Override
    public boolean support(Object obj) {
        return obj instanceof String;
    }

    @Override
    public void resolve(PreparedStatement preparedStatement, int index, String data)
        throws SQLException {
        preparedStatement.setString(index, data);
    }
}
