package com.interface21.jdbc.core.querybuilder.select;

import java.util.List;
import java.util.Optional;

public interface SelectExecutableStep<T> {
    SelectExecutableStep<T> param(Object param);
    List<T> toList();
    Optional<T> findFirst();
}
