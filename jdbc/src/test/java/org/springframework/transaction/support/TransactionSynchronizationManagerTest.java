package org.springframework.transaction.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.mock;

class TransactionSynchronizationManagerTest {

    @Test
    @DisplayName("커넥션을 저장할 수 있다")
    void bindResource() {
        //given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        //when, then
        assertDoesNotThrow(() -> TransactionSynchronizationManager.bindResource(dataSource, connection));
    }

    @Test
    @DisplayName("이미 존재하는 커넥션이 있을 때 저장하면 예외가 발생한다")
    void bindResource_fail() {
        //given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        //when, then
        assertThatThrownBy(() -> TransactionSynchronizationManager.bindResource(dataSource, connection))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 커넥션이 존재합니다.");
    }

    @Test
    @DisplayName("커넥션을 가져올 수 있다")
    void getResource() {
        //given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        //when
        final Connection resource = TransactionSynchronizationManager.getResource(dataSource);

        //then
        assertThat(resource).isEqualTo(connection);
    }

    @Test
    @DisplayName("커넥션을 제거할 수 있다")
    void unbindResource() {
        //given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        //when, then
        assertDoesNotThrow(() -> TransactionSynchronizationManager.unbindResource(dataSource));
    }

    @Test
    @DisplayName("커넥션을 제거할 때 이미 존재하지 않으면 예외가 발생한다")
    void unbindResource_fail() {
        //given
        final DataSource dataSource = mock(DataSource.class);

        //when, then
        assertThatThrownBy(() -> TransactionSynchronizationManager.unbindResource(dataSource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("커넥션이 존재하지 않습니다.");
    }
}
