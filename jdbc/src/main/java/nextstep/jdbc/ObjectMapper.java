package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectMapper {

    public static <T> T mappedObject(
        ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        if (resultSet.next()) {
            return rowMapper.rowMappedObject(resultSet);
        }
        return null;
    }
}
