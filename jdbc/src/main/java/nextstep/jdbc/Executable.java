package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface Executable<T> {

    T execute(ResultSet resultSet, PreparedStatement preparedStatement) throws SQLException;
}
