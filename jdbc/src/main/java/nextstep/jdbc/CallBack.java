package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface CallBack<T> {
    T execute(PreparedStatement pstm) throws SQLException;
}
