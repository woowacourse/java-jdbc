package nextstep.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ObjectMapper<T> {
    T mapObject(ResultSet resultSet) throws SQLException;
}
