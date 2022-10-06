package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    PreparedStatement setPreparedStatement(final Connection connection) throws SQLException;
}
