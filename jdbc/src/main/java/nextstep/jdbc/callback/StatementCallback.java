package nextstep.jdbc.callback;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallback {

	void prepare(PreparedStatement statement) throws SQLException;
}
