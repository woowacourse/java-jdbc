package com.interface21.jdbc.core.querybuilder.select;

import java.util.List;
import java.util.Optional;

public interface SelectWhereStep<T> {
    SelectWhereStep<T> where(String column, Object value); // AND 조건으로 추가

    List<T> toList();

    Optional<T> findFirst();
}
