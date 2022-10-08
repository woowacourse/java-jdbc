package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ExecuteCallBack<T> {

    T action(final PreparedStatement statement) throws SQLException;
}
