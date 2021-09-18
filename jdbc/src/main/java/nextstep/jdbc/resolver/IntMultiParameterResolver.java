package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IntMultiParameterResolver implements MultiParameterResolver {

    @Override
    public <T> boolean support(T t) {
        return t instanceof Integer;
    }

    @Override
    public <T> void resolve(PreparedStatement preparedStatement, int index, T data)
        throws SQLException {
        preparedStatement.setInt(index, (int) data);
    }
}
