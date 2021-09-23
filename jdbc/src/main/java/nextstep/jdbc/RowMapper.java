package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    Object mapRow(ResultSet rs) throws SQLException;
}
