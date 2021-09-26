package nextstep.jdbc.utils;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetExtractor<T> {

    T extractData(ResultSet rs);
}
