package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.exception.ConnectionBindingException;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TransactionSynchronizationManagerTest {

    @Test
    void DataSource에_해당하는_커넥션을_등록한다() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNotNull();
    }

    @Test
    void DataSource에_등록된_커넥션을_등록하면_예외가_발생한다() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, connection))
                .isInstanceOf(ConnectionBindingException.class)
                .hasMessage("커넥션이 이미 바인딩되어 있습니다.");
    }

    @Test
    void DataSource에_해당하는_커넥션을_조회한다() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNotNull();
    }

    @Test
    void DataSource에_해당하는_커넥션이_없으면_NULL_반환한다() {
        DataSource dataSource = mock(DataSource.class);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void DataSource에_해당하는_커넥션을_삭제한다() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        TransactionSynchronizationManager.unbindResource(dataSource);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    void 존재하지_않는_DataSource에_해당하는_커넥션을_삭제하면_예외가_발생한다() {
        DataSource dataSource = mock(DataSource.class);

        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(ConnectionBindingException.class)
                .hasMessage("바인딩 된 커넥션이 존재하지 않습니다.");
    }

    @Test
    void 트랜잭션이_진행_중이면_true_반환한다() {
        TransactionSynchronizationManager.setActualTransactionActive(true);

        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
    }

    @Test
    void 트랜잭션이_진행_중이지_않으면_false_반환한다() {
        TransactionSynchronizationManager.setActualTransactionActive(false);

        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }
}
