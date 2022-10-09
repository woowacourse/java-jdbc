package nextstep.jdbc;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setPreparedStatement(PreparedStatement preparedStatement);
}
