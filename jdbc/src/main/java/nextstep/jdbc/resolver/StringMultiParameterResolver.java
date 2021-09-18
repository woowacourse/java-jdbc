package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringMultiParameterResolver implements MultiParameterResolver {

    @Override
    public <T> boolean support(T t) {
        return t instanceof String;
    }

    @Override
    public <T> void resolve(PreparedStatement preparedStatement, int index, T data)
        throws SQLException {
        preparedStatement.setString(index, (String) data);
    }
}
