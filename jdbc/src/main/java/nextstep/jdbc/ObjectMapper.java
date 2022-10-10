package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ObjectMapper<T> {

    @Nullable
    T mapObject(final ResultSet resultSet, final int rowNum) throws SQLException;
}
