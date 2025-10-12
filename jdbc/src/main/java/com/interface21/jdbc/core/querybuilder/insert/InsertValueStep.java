package com.interface21.jdbc.core.querybuilder.insert;

public interface InsertValueStep {
    InsertExecutableStep value(String column, Object value);
}
