package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDaoFailureTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final UserDao userDao = new UserDao(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @Test
    @DisplayName("사용자 저장이 실패하면 SQL과 원인을 보존하고 자원을 해제한다")
    void insert_failure() throws SQLException {
        final var failure = new SQLException("저장 실패");
        when(statement.executeUpdate()).thenThrow(failure);
        final var user = new User("gugu", "password", "gugu@woowahan.com");

        assertThatThrownBy(() -> userDao.insert(user))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("insert into users (account, password, email) values (?, ?, ?)")
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("수정할 값을 바인딩하다 실패하면 원인을 보존하고 자원을 해제한다")
    void update_binding_failure() throws SQLException {
        final var failure = new SQLException("값 바인딩 실패");
        doThrow(failure).when(statement).setString(1, "gugu");
        final var user = new User(1L, "gugu", "password", "gugu@woowahan.com");

        assertThatThrownBy(() -> userDao.update(user))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("update users set account = ?, password = ?, email = ? where id = ?")
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }
}
