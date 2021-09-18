package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface MultiParameterResolver {

    <T> boolean support(T t);

    <T> void resolve(PreparedStatement preparedStatement, int index, T data) throws SQLException;
}
