package com.interface21.jdbc.core.querybuilder.select;

public interface SelectFromStep<T> {
    SelectWhereStep<T> from(String tableName);
}
