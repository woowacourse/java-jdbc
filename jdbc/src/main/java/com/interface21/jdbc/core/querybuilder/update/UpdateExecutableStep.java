package com.interface21.jdbc.core.querybuilder.update;

public interface UpdateExecutableStep {
    UpdateExecutableStep set(String column, Object value);
    UpdateExecutableStep where(String column, Object value);
    void execute();
}
