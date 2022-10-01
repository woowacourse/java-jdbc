package nextstep.jdbc;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowMapper<T> {

    T mapToObject(ResultSet resultSet, int rowNum);
}
