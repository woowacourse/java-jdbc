package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface Callback<T> {

    T call(final PreparedStatement preparedStatement) throws SQLException;
}
