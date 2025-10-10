package com.interface21.jdbc.core;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class TransactionManagerHolder {

    private static final Map<DataSource, TransactionManager> transactionManagerMap = new HashMap<>();

    public static void add(DataSource key, TransactionManager value) {
        transactionManagerMap.put(key, value);
    }

    public static TransactionManager get(DataSource key) {
        return transactionManagerMap.get(key);
    }
}
