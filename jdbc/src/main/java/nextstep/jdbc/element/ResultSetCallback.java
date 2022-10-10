package nextstep.jdbc.element;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetCallback<T> {

    T execute(ResultSet resultSet) throws SQLException;
}
