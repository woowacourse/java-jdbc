package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.util.Objects;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TransactionSynchronizationManagerTest {

    @DisplayName("DataSource로 Connection을 찾는다.")
    @Test
    void testGetResource() {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // when
        final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isEqualTo(connection);
    }

    @DisplayName("DataSource에 바인딩된 Connection이 없으면, null을 반환한다.")
    @Test
    void testGetResource_ReturnNull_WhenNotBinding() {
        // given
        final DataSource dataSource = mock(DataSource.class);

        // when
        final Connection actual = TransactionSynchronizationManager.getResource(dataSource);

        // then
        assertThat(actual).isNull();
    }

    @DisplayName("DataSource와 Connection을 바인딩한다. (ResourceMap이 초기화되지 않았어도, 에러를 발생하지 않는다.)")
    @Test
    void testBindResource() {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        // when
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        // then
        assertDoesNotThrow(() -> {
            final Connection actual = TransactionSynchronizationManager.getResource(dataSource);
            assertThat(actual).isEqualTo(connection);
        });
    }

    @DisplayName("DataSource의 바인딩을 해제한다.")
    @Test
    void testUnbindResource() {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assert Objects.nonNull(TransactionSynchronizationManager.getResource(dataSource));

        // when
        TransactionSynchronizationManager.unbindResource(dataSource);

        // then
        final Connection actual = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(actual).isNull();
    }

    @DisplayName("DataSource에 바인딩된 Connection이 없어도, 에러를 발생하지 않는다.")
    @Test
    void testUnbindResource_NotThrowError_WhenNotBinding() {
        // given
        final DataSource dataSource = mock(DataSource.class);

        // when & then
        assertDoesNotThrow(() -> TransactionSynchronizationManager.unbindResource(dataSource));
    }
}
