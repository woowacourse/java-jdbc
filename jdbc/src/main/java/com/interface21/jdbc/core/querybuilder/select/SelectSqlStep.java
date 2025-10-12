package com.interface21.jdbc.core.querybuilder.select;

public interface SelectSqlStep<T> {
    SelectExecutableStep<T> sql(String sql);
}
