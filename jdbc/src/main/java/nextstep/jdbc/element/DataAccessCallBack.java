package nextstep.jdbc.element;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DataAccessCallBack<T> {

    T execute(PreparedStatement statement) throws SQLException;
}
