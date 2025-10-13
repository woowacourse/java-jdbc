package com.interface21.jdbc.core;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionCallBack<R> {

    R execute(Connection connection) throws Exception;
}
