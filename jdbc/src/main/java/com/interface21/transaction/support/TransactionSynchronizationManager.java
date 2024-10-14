package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
        System.out.println(key + "의 key = " + value);
        resources.get().put(key, value);
    }

    public static Connection unbindResource(DataSource key) {
        System.out.println(key + "의 key = " + resources.get().get(key));
        return resources.get().remove(key);
    }
}
