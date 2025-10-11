package com.interface21.jdbc.core.querybuilder;

public interface SqlStep<T> {
    SelectExecutableStep<T> sql(String sql);
}
