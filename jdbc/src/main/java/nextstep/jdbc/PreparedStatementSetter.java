package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementSetter {

    PreparedStatement createPreparedStatement(final Connection connection) throws SQLException;
}
