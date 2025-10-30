package com.interface21.jdbc.core.querybuilder.select;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface SelectExecutableStep<T> {
    SelectExecutableStep<T> param(Object param);
    List<T> toList(Connection connection);
    Optional<T> findFirst(Connection connection);
}
