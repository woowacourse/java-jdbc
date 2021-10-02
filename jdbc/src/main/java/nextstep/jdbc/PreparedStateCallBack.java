package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStateCallBack<T> {

    T doAction(PreparedStatement pstmt) throws SQLException;
}
