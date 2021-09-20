package nextstep.jdbc.utils.statement;

import java.sql.SQLException;
import java.sql.Statement;

@FunctionalInterface
public interface StatementCallback<T> {

    T getResult(Statement stmt) throws SQLException;
}
