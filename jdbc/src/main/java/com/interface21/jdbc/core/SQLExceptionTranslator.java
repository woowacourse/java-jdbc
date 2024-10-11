package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import javax.annotation.Nullable;

public interface SQLExceptionTranslator {
    DataAccessException translate(String task, @Nullable String sql, SQLException ex);
}
