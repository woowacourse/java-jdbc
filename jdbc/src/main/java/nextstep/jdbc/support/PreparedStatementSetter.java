package nextstep.jdbc.support;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementSetter {

    void set(PreparedStatement preparedStatement);
}
