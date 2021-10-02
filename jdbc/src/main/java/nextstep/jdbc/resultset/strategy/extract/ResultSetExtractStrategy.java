package nextstep.jdbc.resultset.strategy.extract;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetExtractStrategy<T> {
    T apply(ResultSet resultSet) throws SQLException;
}
