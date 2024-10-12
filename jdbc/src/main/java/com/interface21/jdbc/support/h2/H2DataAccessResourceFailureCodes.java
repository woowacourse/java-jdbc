package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataAccessResourceFailureException;
import com.interface21.jdbc.support.AbstractErrorCodes;

class H2DataAccessResourceFailureCodes extends AbstractErrorCodes {

    public H2DataAccessResourceFailureCodes() {
        super(List.of(90046, 90100, 90117, 90121, 90126));
    }

    @Override
    public Class<? extends DataAccessException> translateTargetClass() {
        return DataAccessResourceFailureException.class;
    }
}
