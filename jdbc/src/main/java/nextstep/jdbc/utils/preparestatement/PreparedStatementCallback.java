package nextstep.jdbc.utils.preparestatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<T> {

    T getResult(PreparedStatement ps) throws SQLException;
}
