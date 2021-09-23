package nextstep.jdbc;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface PreparedStatementSetter {

    void setValue(PreparedStatement ps);
}
