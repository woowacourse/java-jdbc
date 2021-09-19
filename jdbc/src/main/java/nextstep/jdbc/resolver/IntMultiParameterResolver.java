package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IntMultiParameterResolver implements MultiParameterResolver<Integer> {

    @Override
    public boolean support(Object obj) {
        return obj instanceof Integer;
    }

    @Override
    public void resolve(PreparedStatement preparedStatement, int index, Integer data)
        throws SQLException {
        preparedStatement.setInt(index, data);
    }
}
