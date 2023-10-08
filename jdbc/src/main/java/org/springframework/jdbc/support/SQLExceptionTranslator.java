package org.springframework.jdbc.support;

import java.sql.SQLException;
import org.springframework.dao.DataAccessException;

public class SQLExceptionTranslator {

    private SQLExceptionTranslator() {
        
    }

    public static DataAccessException translate(String sql,  SQLException e) {
        Class<? extends DataAccessException> exceptionClazz =
                H2SQLErrorCodeToDataAccessExceptionMapper.mapSQLErrorCode(e.getErrorCode());

        try {
            return exceptionClazz.getDeclaredConstructor(String.class, SQLException.class)
                    .newInstance(sql, e);
        } catch (ReflectiveOperationException ex) {
            return new DataAccessException("Some exception was thrown : " + e.getMessage());
        }
    }
}
