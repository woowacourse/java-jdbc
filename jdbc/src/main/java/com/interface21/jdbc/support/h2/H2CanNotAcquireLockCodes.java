package com.interface21.jdbc.support.h2;

import java.sql.SQLException;
import java.util.List;
import com.interface21.dao.CannotAcquireLockException;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.support.AbstractErrorCodes;

class H2CanNotAcquireLockCodes extends AbstractErrorCodes {

    public H2CanNotAcquireLockCodes() {
        super(List.of(50200));
    }

    @Override
    public DataAccessException translate(SQLException sqlException) {
        return new CannotAcquireLockException(sqlException);
    }
}