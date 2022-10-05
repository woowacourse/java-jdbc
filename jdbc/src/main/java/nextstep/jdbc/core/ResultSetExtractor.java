package nextstep.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.DataAccessException;

@FunctionalInterface
public interface ResultSetExtractor<T> {

    T extractData(ResultSet rs) throws SQLException, DataAccessException;

}
