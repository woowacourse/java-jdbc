package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

@FunctionalInterface
public interface TransactionCallBack<R> {

    R execute() throws DataAccessException;
}
