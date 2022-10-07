package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementGenerator {

    PreparedStatement generate(Connection connection) throws SQLException;
}
