package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.dao.CannotAcquireLockException;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.support.AbstractErrorCodes;

class H2CanNotAcquireLockCodes extends AbstractErrorCodes {

    public H2CanNotAcquireLockCodes() {
        super(List.of(50200));
    }

    @Override
    public Class<? extends DataAccessException> translateTargetClass() {
        return CannotAcquireLockException.class;
    }
}
