package nextstep.jdbc.connector;

import java.sql.Connection;

public interface DbConnector {

    Connection getConnection();
}
