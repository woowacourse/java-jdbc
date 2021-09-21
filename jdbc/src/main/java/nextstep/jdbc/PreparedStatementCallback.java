package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCallback<T> {

    T doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException;
}
