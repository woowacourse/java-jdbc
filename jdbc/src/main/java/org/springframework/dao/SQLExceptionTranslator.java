package org.springframework.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.BatchUpdateException;
import java.sql.DataTruncation;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLTransientException;
import java.sql.SQLWarning;
import java.util.HashMap;
import java.util.Map;
import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.serial.SerialException;
import javax.sql.rowset.spi.SyncFactoryException;
import javax.sql.rowset.spi.SyncProviderException;

public class SQLExceptionTranslator {

    public static final Map<Class<? extends SQLException>, Class<? extends DataAccessException>> exceptions = new HashMap<>();

    static {
        exceptions.put(BatchUpdateException.class, org.springframework.dao.BatchUpdateException.class);
        exceptions.put(DataTruncation.class, org.springframework.dao.DataTruncation.class);
        exceptions.put(RowSetWarning.class, org.springframework.dao.RowSetWarning.class);
        exceptions.put(SerialException.class, org.springframework.dao.SerialException.class);
        exceptions.put(SQLClientInfoException.class, org.springframework.dao.SQLClientInfoException.class);
        exceptions.put(SQLDataException.class, org.springframework.dao.SQLDataException.class);
        exceptions.put(SQLFeatureNotSupportedException.class, org.springframework.dao.SQLFeatureNotSupportedException.class);
        exceptions.put(SQLIntegrityConstraintViolationException.class, org.springframework.dao.SQLIntegrityConstraintViolationException.class);
        exceptions.put(SQLInvalidAuthorizationSpecException.class, org.springframework.dao.SQLIntegrityConstraintViolationException.class);
        exceptions.put(SQLNonTransientConnectionException.class, org.springframework.dao.SQLNonTransientConnectionException.class);
        exceptions.put(SQLNonTransientException.class, org.springframework.dao.SQLNonTransientException.class);
        exceptions.put(SQLRecoverableException.class, org.springframework.dao.SQLRecoverableException.class);
        exceptions.put(SQLSyntaxErrorException.class, org.springframework.dao.SQLSyntaxErrorException.class);
        exceptions.put(SQLTimeoutException.class, org.springframework.dao.SQLTimeoutException.class);
        exceptions.put(SQLTransactionRollbackException.class, org.springframework.dao.SQLTransactionRollbackException.class);
        exceptions.put(SQLTransientConnectionException.class, org.springframework.dao.SQLTransientConnectionException.class);
        exceptions.put(SQLTransientException.class, org.springframework.dao.SQLTransientException.class);
        exceptions.put(SQLWarning.class, org.springframework.dao.SQLWarning.class);
        exceptions.put(SyncFactoryException.class, org.springframework.dao.SyncFactoryException.class);
        exceptions.put(SyncProviderException.class, org.springframework.dao.SyncProviderException.class);
    }

    public static DataAccessException translate(SQLException sqlException) {
        final Class<? extends DataAccessException> dataAccessException = exceptions.get(sqlException.getClass());
        try {
            final Constructor<? extends DataAccessException> constructor = dataAccessException.getDeclaredConstructor(Throwable.class);
            return constructor.newInstance(sqlException);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DataAccessException(sqlException);
        }
    }
}
