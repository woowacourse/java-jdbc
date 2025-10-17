package com.interface21.transaction;

import com.interface21.dao.DataAccessException;

public interface PlatformTransactionManager {

    void commit() throws DataAccessException;

    void rollback() throws DataAccessException;
}
