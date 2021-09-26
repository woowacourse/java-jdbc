package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface QueryCallBack<T> {
    T execute(PreparedStatement preparedStatement) throws SQLException;
}
