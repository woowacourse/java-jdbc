package nextstep.jdbc.test;

import nextstep.jdbc.utils.TransactionManager;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class Rollback implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        TransactionManager.startTransaction();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        TransactionManager.rollback();
    }
}
