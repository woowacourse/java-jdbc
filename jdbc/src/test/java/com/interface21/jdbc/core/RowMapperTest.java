package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.interface21.jdbc.support.TestUser;

class RowMapperTest {

    @Test
    void mapRow() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);

        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("John Doe");

        RowMapper<TestUser> rowMapper = new TestUserRowMapper();
        TestUser testUser = rowMapper.mapRow(resultSet, 1);

        assertThat(testUser.getId()).isEqualTo(1);
        assertThat(testUser.getName()).isEqualTo("John Doe");
    }
}

class TestUserRowMapper implements RowMapper<TestUser> {

    @Override
    public TestUser mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new TestUser(resultSet.getLong("id"), resultSet.getString("name"));
    }
}
