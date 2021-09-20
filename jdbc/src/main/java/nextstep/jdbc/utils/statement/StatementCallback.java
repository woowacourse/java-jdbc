package nextstep.jdbc.utils.statement;

import java.sql.SQLException;
import java.sql.Statement;

public interface StatementCallback<T> {

    T getResult(Statement stmt) throws SQLException;
}
