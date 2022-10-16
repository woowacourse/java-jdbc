package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ResultSetExtractor<T> {
    T extractData(ResultSet rs) throws SQLException, DataAccessException;
    List<T> extractList(ResultSet rs) throws SQLException, DataAccessException;
}
