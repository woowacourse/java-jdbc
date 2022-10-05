package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface Executable<T> {

    List<T> execute(final PreparedStatement statement) throws SQLException;
}
