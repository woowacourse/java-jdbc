package nextstep.jdbc;

import java.sql.ResultSet;
import java.util.List;

@FunctionalInterface
public interface ResultExtractor<T> {

    List<T> extractResult(final ResultSet results);
}
