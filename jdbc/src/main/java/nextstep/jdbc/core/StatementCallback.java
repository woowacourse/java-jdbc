package nextstep.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementCallback<T> {
    T executeQuery(PreparedStatement statement) throws SQLException;
}
