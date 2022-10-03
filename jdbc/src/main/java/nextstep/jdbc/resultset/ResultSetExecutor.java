package nextstep.jdbc.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.exception.DataAccessException;

@FunctionalInterface
public interface ResultSetExecutor<T> {

    T extractData(ResultSet resultSet) throws SQLException, DataAccessException;
}
