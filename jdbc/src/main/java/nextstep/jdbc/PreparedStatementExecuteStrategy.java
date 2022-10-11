package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementExecuteStrategy<T> {

    T extract(final PreparedStatement preparedStatement) throws SQLException;
}
