package org.springframework.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class TransactionSynchronizationManagerTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);

    @Test
    void Connection을_가져온다() throws Exception {
        // given
        when(dataSource.getConnection()).thenReturn(connection);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection result = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(connection);
    }

    @Test
    void Connection이_없으면_null이_반환된다() {
        // when
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(connection).isNull();
    }

    @Test
    void Connection을_쓰레드로컬에_할당한다() {
        assertThatNoException().isThrownBy(
                () -> TransactionSynchronizationManager.bindResource(dataSource, connection)
        );
    }

    @Test
    void Connection을_쓰레드로컬에서_할당_해제한다() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        Connection result = TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        assertThat(result).isEqualTo(connection);
    }

    @Test
    void Connection을_쓰레드로컬에서_할당_해제하고_Get하면_null을_반환한다() {
        // given
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        TransactionSynchronizationManager.unbindResource(dataSource);
        Connection result = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 할당_해제할_때_할당된_자원이_없으면_예외가_발생한다() {
        // expect
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("There is no resource");
    }
}
