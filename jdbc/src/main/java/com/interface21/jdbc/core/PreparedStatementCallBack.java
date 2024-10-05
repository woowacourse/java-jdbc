package com.interface21.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCallBack {

    void callback(PreparedStatement preparedStatement) throws SQLException;
}
