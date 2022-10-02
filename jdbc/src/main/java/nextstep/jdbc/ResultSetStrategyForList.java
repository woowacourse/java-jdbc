package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ResultSetStrategyForList {

    List<Object> mapRows(ResultSet resultSet) throws SQLException;
}
