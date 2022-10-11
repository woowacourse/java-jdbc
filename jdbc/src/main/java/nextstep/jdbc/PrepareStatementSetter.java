package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PrepareStatementSetter {

    void setParams(PreparedStatement preparedStatement) throws SQLException;
}
