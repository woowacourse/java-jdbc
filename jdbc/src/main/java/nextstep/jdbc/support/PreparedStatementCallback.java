package nextstep.jdbc.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T doInStatement(PreparedStatement preparedStatement) throws SQLException;
}
