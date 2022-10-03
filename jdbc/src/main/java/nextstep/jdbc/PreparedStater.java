package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStater<T> {

    T doStatement(PreparedStatement preparedStatement) throws SQLException;
}