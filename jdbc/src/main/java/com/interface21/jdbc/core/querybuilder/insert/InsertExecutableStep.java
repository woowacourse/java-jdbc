package com.interface21.jdbc.core.querybuilder.insert;

public interface InsertExecutableStep {
    InsertExecutableStep value(String column, Object value);
    void execute();
}
