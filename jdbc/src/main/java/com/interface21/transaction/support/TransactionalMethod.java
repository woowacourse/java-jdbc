package com.interface21.transaction.support;

import java.sql.Connection;

@FunctionalInterface
public interface TransactionalMethod<R> {

    R method(Connection connection) throws Exception;
}
