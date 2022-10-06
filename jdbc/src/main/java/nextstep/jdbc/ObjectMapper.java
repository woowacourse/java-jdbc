package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nullable;

@FunctionalInterface
public interface ObjectMapper<T> {

    T mapObject(final ResultSet resultSet) throws SQLException;
}
