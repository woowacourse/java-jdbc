package nextstep.jdbc;

import java.sql.ResultSet;
import java.util.List;

public interface JdbcMapper<T> {

    List<T> mapRow(ResultSet resultSet);
}
