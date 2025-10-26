package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;

@FunctionalInterface
public interface TransactionCallBack<R> {

    R execute(Connection connection) throws DataAccessException;
}
