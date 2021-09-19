package com.techcourse.dao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("try-with-resources close 테스트")
public class UserDaoCloseTest {

    @DisplayName("createPreparedStatement 를 사용할 때, close 가 호출되는지 확인한다.")
    @Test
    void close() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        UserDao userDao = new UserDao(dataSource);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);

        userDao.findByAccount("gugu");

        verify(conn).close();
        verify(pstmt).close();
        verify(rs).close();
    }
}
