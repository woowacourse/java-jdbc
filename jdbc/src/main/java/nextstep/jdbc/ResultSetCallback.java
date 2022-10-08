package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ResultSetCallback<T> {

    T doResultSet(PreparedStatement preparedStatement) throws SQLException;
}
