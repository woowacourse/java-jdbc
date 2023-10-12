package org.springframework.transaction.support;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.mockito.Mockito.mock;
import static org.springframework.transaction.support.TransactionSynchronizationManager.*;

class TransactionSynchronizationManagerTest {

    DataSource dataSource = mock(DataSource.class);
    Connection connection = mock(Connection.class);

    @Test
    void 커넥션이_null이라면_null을_반환한다() {
        Connection resource = getResource(dataSource);
        Assertions.assertThat(resource).isNull();
    }

    @Test
    void 커넥션을_반환한다() {
        bindResource(dataSource, connection);
        Connection resource = getResource(dataSource);
        Assertions.assertThat(resource).isNotNull();
    }

    @Test
    void 커넥션을_바인딩한다() {
        bindResource(dataSource, connection);
        Connection resource = getResource(dataSource);
        Assertions.assertThat(resource).isNotNull();
    }

}