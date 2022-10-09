package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ResultSetExtractor<T> {

    List<T> extract(final ResultSet resultSet) throws SQLException;
}
