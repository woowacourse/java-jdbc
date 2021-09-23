package nextstep.jdbc;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {
    @Nullable
    T map(ResultSet resultSet, int rowNum) throws SQLException;
}
