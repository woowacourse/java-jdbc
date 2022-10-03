package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
interface ExtractStrategy<T> {

    List<T> extract(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException;
}
