package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementCallBack<T> {

    T apply(final PreparedStatement statement) throws SQLException;
}
