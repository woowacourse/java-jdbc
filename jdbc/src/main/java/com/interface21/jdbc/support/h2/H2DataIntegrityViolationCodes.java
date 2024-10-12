package com.interface21.jdbc.support.h2;

import java.util.List;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataIntegrityViolationException;
import com.interface21.jdbc.support.AbstractErrorCodes;

class H2DataIntegrityViolationCodes extends AbstractErrorCodes {

    public H2DataIntegrityViolationCodes() {
        super(List.of(22001, 22003, 22012, 22018, 22025, 23000, 23002, 23003, 23502, 23503, 23506, 23507, 23513));
    }

    @Override
    public Class<? extends DataAccessException> translateTargetClass() {
        return DataIntegrityViolationException.class;
    }
}
