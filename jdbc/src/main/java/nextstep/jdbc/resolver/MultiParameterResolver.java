package nextstep.jdbc.resolver;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface MultiParameterResolver<T> {

    boolean support(Object obj);

    void resolve(PreparedStatement preparedStatement, int index, T data) throws SQLException;
}
