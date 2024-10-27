package com.interface21.transaction.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransactionSynchronizationManagerTest {

    @Nested
    @DisplayName("resource 조회")
    class GetResource {

        @Test
        @DisplayName("성공 : 값이 존재할 경우")
        void getResourceWithExists() {
            DataSource dataSource = Mockito.mock(DataSource.class);
            Connection connection = Mockito.mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            Connection actual = TransactionSynchronizationManager.getResource(dataSource);

            assertThat(actual).isEqualTo(connection);
        }

        @Test
        @DisplayName("성공 : 값이 존재하지 않을 경우")
        void getResourceWithNotExists() {
            DataSource dataSource = Mockito.mock(DataSource.class);

            Connection actual = TransactionSynchronizationManager.getResource(dataSource);

            assertThat(actual).isNull();
        }
    }

    @Test
    @DisplayName("resource 추가 성공")
    void bindResource() {
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);

        Connection actual = TransactionSynchronizationManager.getResource(dataSource);
        assertThat(actual).isEqualTo(connection);
    }


    @Nested
    @DisplayName("resource 삭제 및 조회")
    class UnbindResource {

        @Test
        @DisplayName("성공 : 값이 존재할 경우")
        void unbindResourceWithExists() {
            DataSource dataSource = Mockito.mock(DataSource.class);
            Connection connection = Mockito.mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            Connection unbindBefore = TransactionSynchronizationManager.unbindResource(dataSource);
            Connection unbindAfter = TransactionSynchronizationManager.unbindResource(dataSource);

            assertAll(
                    () -> assertThat(unbindBefore).isEqualTo(connection),
                    () -> assertThat(unbindAfter).isNull()
            );
        }

        @Test
        @DisplayName("성공 : 값이 존재하지 않을 경우")
        void unbindResourceWithNotExists() {
            DataSource dataSource = Mockito.mock(DataSource.class);

            Connection unbindBefore = TransactionSynchronizationManager.unbindResource(dataSource);
            Connection unbindAfter = TransactionSynchronizationManager.unbindResource(dataSource);

            assertAll(
                    () -> assertThat(unbindBefore).isNull(),
                    () -> assertThat(unbindAfter).isNull()
            );
        }
    }
}
