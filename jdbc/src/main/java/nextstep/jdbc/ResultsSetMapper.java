package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface ResultsSetMapper<T> {

    List<T> collect(ResultSet resultSet) throws SQLException;
}
