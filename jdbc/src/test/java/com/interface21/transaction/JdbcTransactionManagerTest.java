package com.interface21.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JdbcTransactionManagerTest {

    private DataSource dataSource;
    private Connection connection;
    private TransactionManager manager;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        given(dataSource.getConnection()).willReturn(connection);
        manager = new JdbcTransactionManager(dataSource);
    }

    @Test
    void 두_SQL을_같은_연결에서_실행한_뒤_커밋하고_DB_연결을_닫는다() throws SQLException {
        // given
        PreparedStatement statement = mock(PreparedStatement.class);
        given(connection.prepareStatement(anyString())).willReturn(statement);
        given(statement.executeUpdate()).willReturn(1);

        // when
        manager.execute(jdbc -> {
            assertThat(jdbc.update("first")).isEqualTo(1);
            assertThat(jdbc.update("second")).isEqualTo(1);
        });

        // then
        InOrder order = inOrder(dataSource, connection, statement);
        order.verify(dataSource).getConnection();
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).prepareStatement("first");
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).prepareStatement("second");
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).commit();
        order.verify(connection).close();
        then(connection).should(never()).rollback();
    }

    @Test
    void 작업에_실패하면_롤백하고_원래_예외를_전달한다() throws SQLException {
        // given
        RuntimeException failure = new IllegalStateException("work failed");

        // when & then
        assertThatThrownBy(() -> manager.execute(jdbc -> {
            throw failure;
        })).isSameAs(failure);
        then(connection).should().rollback();
        then(connection).should(never()).commit();
        then(connection).should().close();
    }

    @Test
    void SQL_실행에_실패하면_롤백하고_DB_오류_원인을_보존한다() throws SQLException {
        // given
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException failure = new SQLException("SQL failed");
        given(connection.prepareStatement("sql")).willReturn(statement);
        given(statement.executeUpdate()).willThrow(failure);

        // when & then
        assertThatThrownBy(() -> manager.execute(jdbc -> jdbc.update("sql")))
            .isInstanceOf(DataAccessException.class)
            .hasCause(failure);
        then(statement).should().close();
        then(connection).should().rollback();
        then(connection).should(never()).commit();
        then(connection).should().close();
    }

    @Test
    void 커밋에_실패하면_롤백을_시도하고_원인을_전달한다() throws SQLException {
        // given
        SQLException failure = new SQLException("commit failed");
        doThrow(failure).when(connection).commit();

        // when & then
        assertThatThrownBy(() -> manager.execute(jdbc -> {}))
            .isInstanceOf(DataAccessException.class)
            .hasCause(failure);
        InOrder order = inOrder(connection);
        order.verify(connection).commit();
        order.verify(connection).rollback();
        order.verify(connection).close();
    }

    @Test
    void 롤백에도_실패하면_두_오류를_보존하고_DB_연결을_닫는다() throws SQLException {
        // given
        RuntimeException workFailure = new IllegalStateException("work failed");
        SQLException rollbackFailure = new SQLException("rollback failed");
        doThrow(rollbackFailure).when(connection).rollback();

        // when & then
        assertThatThrownBy(() -> manager.execute(jdbc -> {
            throw workFailure;
        })).isSameAs(workFailure);
        assertThat(workFailure.getSuppressed()).containsExactly(rollbackFailure);
        then(connection).should().rollback();
        then(connection).should(never()).commit();
        then(connection).should().close();
    }

    @Test
    void 작업_중_심각한_오류가_발생해도_롤백하고_오류를_전달한다() throws SQLException {
        // given
        Error failure = new AssertionError("work failed");

        // when & then
        assertThatThrownBy(() -> manager.execute(jdbc -> {
            throw failure;
        })).isSameAs(failure);
        then(connection).should().rollback();
        then(connection).should(never()).commit();
        then(connection).should().close();
    }

    @Test
    void 연결을_얻지_못하면_작업을_실행하지_않고_원인을_전달한다() throws SQLException {
        // given
        SQLException failure = new SQLException("connection failed");
        given(dataSource.getConnection()).willThrow(failure);
        TransactionWork work = mock(TransactionWork.class);

        // when & then
        assertThatThrownBy(() -> manager.execute(work))
            .isInstanceOf(DataAccessException.class)
            .hasCause(failure);
        then(work).shouldHaveNoInteractions();
    }

    @Test
    void 자동_커밋을_끄지_못하면_작업을_실행하지_않고_연결을_닫는다() throws SQLException {
        // given
        SQLException failure = new SQLException("auto commit setting failed");
        doThrow(failure).when(connection).setAutoCommit(false);
        TransactionWork work = mock(TransactionWork.class);

        // when & then
        assertThatThrownBy(() -> manager.execute(work))
            .isInstanceOf(DataAccessException.class)
            .hasCause(failure);
        then(work).shouldHaveNoInteractions();
        then(connection).should(never()).commit();
        then(connection).should(never()).rollback();
        then(connection).should().close();
    }

    @Test
    void 커밋_후_연결_종료가_실패하면_오류를_전달하고_롤백하지_않는다() throws SQLException {
        // given
        SQLException failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        // when & then
        assertThatThrownBy(() -> manager.execute(jdbc -> {}))
            .isInstanceOf(DataAccessException.class)
            .hasCause(failure);
        then(connection).should().commit();
        then(connection).should(never()).rollback();
    }
}
