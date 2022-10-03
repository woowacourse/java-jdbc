package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface Executable<T> {

    T execute(PreparedStatement preparedStatement) throws SQLException;
}
