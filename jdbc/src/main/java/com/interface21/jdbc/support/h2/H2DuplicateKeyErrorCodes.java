package com.interface21.jdbc.support.h2;

import java.sql.SQLException;
import java.util.List;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.DuplicateKeyException;
import com.interface21.jdbc.support.AbstractErrorCodes;

class H2DuplicateKeyErrorCodes extends AbstractErrorCodes {

    public H2DuplicateKeyErrorCodes() {
        super(List.of(23001, 23505));
    }

    @Override
    public DataAccessException translate(SQLException sqlException) {
        return new DuplicateKeyException(sqlException);
    }
}
