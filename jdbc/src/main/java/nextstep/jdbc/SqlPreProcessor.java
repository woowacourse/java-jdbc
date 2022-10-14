package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlPreProcessor {

    PreparedStatement preProcess(Connection conn) throws SQLException;
}
