package nextstep.jdbc;

import java.sql.Connection;
import java.util.List;

public interface ResultSetExecutor<T> {

    List<T> execute(final Connection connection);
}
