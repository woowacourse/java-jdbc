package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper {

    <T> T rowMappedObject(ResultSet resultSet) throws SQLException;
}
