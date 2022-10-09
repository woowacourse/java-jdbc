package nextstep.jdbc.callback;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapperCallback {

	Object map(ResultSet rs) throws SQLException;
}
