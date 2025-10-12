
package com.interface21.jdbc.core.querybuilder.delete;

public interface DeleteWhereStep {
    DeleteExecutableStep where(String column, Object value);
}
