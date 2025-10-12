
package com.interface21.jdbc.core.querybuilder.delete;

public interface DeleteExecutableStep {
    DeleteExecutableStep where(String column, Object value);
    void execute();
}
