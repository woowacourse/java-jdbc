package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(final PreparedStatement ps) throws SQLException;
}
