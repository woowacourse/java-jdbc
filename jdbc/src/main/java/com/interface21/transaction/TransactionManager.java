package com.interface21.transaction;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TransactionManager {
    
    void doInTransaction(Consumer<Connection> action);
}
