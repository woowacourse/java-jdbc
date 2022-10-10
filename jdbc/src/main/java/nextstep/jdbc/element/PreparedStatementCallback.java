package nextstep.jdbc.element;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T execute(PreparedStatement statement) throws SQLException;
}
