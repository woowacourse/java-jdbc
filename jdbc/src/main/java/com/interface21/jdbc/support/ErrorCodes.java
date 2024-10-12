package com.interface21.jdbc.support;

import com.interface21.dao.DataAccessException;

public interface ErrorCodes {

    boolean contains(int code);

    Class<? extends DataAccessException> translateTargetClass();
}
