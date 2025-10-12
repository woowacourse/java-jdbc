package com.interface21.jdbc.core.querybuilder.update;

public interface UpdateSetStep {
    UpdateExecutableStep set(String column, Object value);
}
