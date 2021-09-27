package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface CallBack<T> {
    T execute(PreparedStatement pstm) throws SQLException;
}
