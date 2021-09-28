package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PrepareStatementAction<T> {

    T execute(PreparedStatement preparedStatement) throws SQLException;
}
