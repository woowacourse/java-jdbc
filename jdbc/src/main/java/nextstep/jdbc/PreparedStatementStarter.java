package nextstep.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface PreparedStatementStarter {

    void setParameters(Object[] parameters) throws SQLException;

    int executeUpdate() throws SQLException;

    ResultSet executeQuery() throws SQLException;
}
