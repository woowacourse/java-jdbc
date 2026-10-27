package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// TransactionSynchronizationManager에 묶여 있다는 건 트랜잭션을 연 쪽이 그 Connection의 주인이라는 뜻이다.
// 묶고 푸는 건 트랜잭션을 연 쪽만 하고, DataSourceUtils는 바인딩을 확인만 한다.
class DataSourceUtilsTest {

    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
    }

    // 매니저의 상태는 static ThreadLocal이라, 테스트가 남긴 바인딩을 지워서 같은 스레드의 다음 테스트로 넘기지 않는다.
    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Nested
    @DisplayName("getConnection은 바인딩을 먼저 보고, 없으면 묶지 않은 새 Connection을 준다")
    class GetConnection {

        @Test
        @DisplayName("트랜잭션 Connection이 묶여 있으면 DataSource에서 새로 얻지 않고 묶인 Connection을 준다")
        void returnsBoundConnection() throws SQLException {
            final Connection transactionConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, transactionConnection);

            final Connection actual = DataSourceUtils.getConnection(dataSource);

            assertThat(actual).isSameAs(transactionConnection);
            verify(dataSource, never()).getConnection();
        }

        @Test
        @DisplayName("묶인 Connection이 없으면 DataSource에서 새로 얻어서 준다")
        void obtainsNewConnectionWhenNotBound() throws SQLException {
            final Connection actual = DataSourceUtils.getConnection(dataSource);

            assertThat(actual).isSameAs(connection);
            verify(dataSource).getConnection();
        }

        @Test
        @DisplayName("묶인 Connection이 없을 때 새로 얻은 Connection은 바인딩하지 않는다")
        void doesNotBindNewConnection() {
            DataSourceUtils.getConnection(dataSource);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        }

        // 톰캣은 스레드를 재사용한다. 트랜잭션 없는 앞 요청이 닫은 Connection을 같은 스레드의 다음 요청이 받으면 안 된다.
        @Test
        @DisplayName("트랜잭션 없이 얻고 반납하기를 반복하면, 매번 DataSource에서 새 Connection을 얻는다")
        void obtainsFreshConnectionEachTimeWithoutTransaction() throws SQLException {
            final Connection nextConnection = mock(Connection.class);
            given(dataSource.getConnection()).willReturn(connection, nextConnection);

            final Connection first = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(first, dataSource);
            final Connection second = DataSourceUtils.getConnection(dataSource);

            assertThat(second).isSameAs(nextConnection);
            verify(dataSource, times(2)).getConnection();
        }

        @Test
        @DisplayName("DataSource가 Connection을 주지 못하면 CannotGetJdbcConnectionException을 던지고 원인을 남긴다")
        void keepsCauseWhenDataSourceFails() throws SQLException {
            final SQLException connectionFailure = new SQLException("연결 실패");
            given(dataSource.getConnection()).willThrow(connectionFailure);

            assertThatThrownBy(() -> DataSourceUtils.getConnection(dataSource))
                    .isInstanceOf(CannotGetJdbcConnectionException.class)
                    .hasCause(connectionFailure);
        }
    }

    @Nested
    @DisplayName("releaseConnection은 묶여 있는 바로 그 Connection이면 손대지 않고, 아니면 닫는다")
    class ReleaseConnection {

        @Test
        @DisplayName("묶여 있는 트랜잭션 Connection은 닫지 않는다")
        void doesNotCloseBoundConnection() throws SQLException {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            DataSourceUtils.releaseConnection(connection, dataSource);

            verify(connection, never()).close();
        }

        // 바인딩을 푸는 건 트랜잭션을 연 쪽의 몫이다.
        @Test
        @DisplayName("묶여 있는 트랜잭션 Connection을 release해도 바인딩을 풀지 않는다")
        void keepsBindingOfBoundConnection() {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            DataSourceUtils.releaseConnection(connection, dataSource);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        }

        @Test
        @DisplayName("묶인 Connection이 없으면 받은 Connection을 닫는다")
        void closesWhenNothingBound() throws SQLException {
            DataSourceUtils.releaseConnection(connection, dataSource);

            verify(connection).close();
        }

        @Test
        @DisplayName("다른 Connection이 묶여 있으면 받은 Connection만 닫고, 묶인 Connection은 건드리지 않는다")
        void closesOnlyGivenConnectionWhenAnotherIsBound() throws SQLException {
            final Connection transactionConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, transactionConnection);

            DataSourceUtils.releaseConnection(connection, dataSource);

            verify(connection).close();
            verify(transactionConnection, never()).close();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(transactionConnection);
        }

        // 트랜잭션을 연 쪽이 커밋이나 롤백을 마치고 바인딩을 푼 뒤에 release하면, 그때는 닫힌다.
        @Test
        @DisplayName("트랜잭션이 끝나 바인딩이 풀린 Connection은 release하면 닫는다")
        void closesAfterTransactionUnbinds() throws SQLException {
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            TransactionSynchronizationManager.unbindResource(dataSource);

            DataSourceUtils.releaseConnection(connection, dataSource);

            verify(connection).close();
        }

        // 지키려는 건 "삼키지 않고 던지되 원인을 남긴다"는 정책이라, 어떤 예외 타입으로 감쌀지는 구현에 맡긴다.
        @Test
        @DisplayName("close가 실패하면 예외를 던지고, 원인 SQLException을 남긴다")
        void keepsCauseWhenCloseFails() throws SQLException {
            final SQLException closeFailure = new SQLException("close 실패");
            willThrow(closeFailure).given(connection).close();

            assertThatThrownBy(() -> DataSourceUtils.releaseConnection(connection, dataSource))
                    .hasCause(closeFailure);
        }
    }

    // 서비스가 트랜잭션 Connection을 묶어두고, DAO는 쿼리마다 Connection을 얻고 반납한다.
    @Nested
    @DisplayName("트랜잭션을 연 쪽과 함께 쓴다")
    class WithTransactionOwner {

        @Test
        @DisplayName("트랜잭션 중에는 쿼리마다 얻고 반납해도 같은 Connection을 받고, 중간에 닫히지 않고, 새로 얻지 않는다")
        void sharesOneConnectionAcrossQueries() throws SQLException {
            final Connection transactionConnection = mock(Connection.class);
            TransactionSynchronizationManager.bindResource(dataSource, transactionConnection);

            final Connection forUpdate = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(forUpdate, dataSource);
            final Connection forHistory = DataSourceUtils.getConnection(dataSource);
            DataSourceUtils.releaseConnection(forHistory, dataSource);

            assertThat(forUpdate).isSameAs(transactionConnection);
            assertThat(forHistory).isSameAs(transactionConnection);
            verify(transactionConnection, never()).close();
            verify(dataSource, never()).getConnection();
        }

        // getConnection이 몰래 묶어버리면, 트랜잭션을 여는 쪽의 bind가 두 번째 bind가 되어
        // TransactionSynchronizationManager의 중복 bind 규칙에 걸려 터진다.
        @Test
        @DisplayName("getConnection이 바인딩을 남기지 않아서, 트랜잭션을 여는 쪽의 첫 bind가 중복 bind로 터지지 않는다")
        void ownerBindIsNotDuplicateAfterGetConnection() {
            final Connection obtained = DataSourceUtils.getConnection(dataSource);

            TransactionSynchronizationManager.bindResource(dataSource, obtained);

            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(obtained);
        }
    }
}
