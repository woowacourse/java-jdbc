package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.exception.DataAccessException;

@FunctionalInterface
public interface ResultSetExtractor<T> {
    T extractData(ResultSet rs) throws SQLException, DataAccessException;
}
