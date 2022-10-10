package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter<T> {

    void setValues(PreparedStatement ps) throws SQLException;
}
