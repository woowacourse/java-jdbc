package org.springframework.jdbc.support;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final List<DataSource> dataSources;

    public TransactionTemplate(final DataSource... dataSource) {
        this.dataSources = Arrays.stream(dataSource)
                .collect(Collectors.toUnmodifiableList());
    }

    public void execute(final TransactionCallBack transactionCallBack) {
        try {
            TransactionManager.beginTransaction();
            transactionCallBack.callbackInTransaction();
            dataSources.forEach(TransactionManager::commit);
        } catch (RuntimeException e) {
            TransactionManager.rollback();
            throw e;
        }
    }
}
