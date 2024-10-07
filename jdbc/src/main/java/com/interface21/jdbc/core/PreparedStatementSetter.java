package com.interface21.jdbc.core;

import java.sql.PreparedStatement;

public interface PreparedStatementSetter {

    void setValues(PreparedStatement preparedStatement);
}
