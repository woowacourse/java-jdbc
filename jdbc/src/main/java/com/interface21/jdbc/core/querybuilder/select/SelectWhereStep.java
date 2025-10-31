package com.interface21.jdbc.core.querybuilder.select;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface SelectWhereStep<T> {

    SelectWhereStep<T> where(String column, Object value); // AND 조건으로 추가

    List<T> toList(Connection connection);

    //JdbcTemplate이 내부적으로 Connection을 획득/반납 (auto-commit)
    List<T> toList();

    Optional<T> findFirst(Connection connection);

    //JdbcTemplate이 내부적으로 Connection을 획득/반납 (auto-commit)
    Optional<T> findFirst();
}
