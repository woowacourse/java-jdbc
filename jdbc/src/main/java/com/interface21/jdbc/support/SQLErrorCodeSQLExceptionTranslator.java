package com.interface21.jdbc.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.SQLExceptionTranslator;
import java.sql.SQLException;
import javax.annotation.Nullable;

public class SQLErrorCodeSQLExceptionTranslator implements SQLExceptionTranslator {

    @Override
    public DataAccessException translate(String task, @Nullable String sql, SQLException ex) {
        return MySQLError.from(ex.getErrorCode())
                .generateException();
    }
}
