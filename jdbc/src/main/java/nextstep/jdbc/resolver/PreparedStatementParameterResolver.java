package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementParameterResolver {

    void resolve(PreparedStatement preparedStatement) throws SQLException;

    static PreparedStatement identity(PreparedStatement preparedStatement){
        return preparedStatement;
    }
}
