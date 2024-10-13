package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.support.TestDomain;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private PreparedStatement mockedPreparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        Connection mockedConnection = mock(Connection.class);
        DataSource mockedDataSource = mock(DataSource.class);
        mockedPreparedStatement = mock(PreparedStatement.class);
        when(mockedDataSource.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(any())).thenReturn(mockedPreparedStatement);
        jdbcTemplate = new JdbcTemplate(mockedDataSource);
    }

    @DisplayName("query 메서드 실행 시 조회한 객체들을 rowMapper로 매핑해서 반환한다.")
    @Test
    void queryTest() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("망쵸", "제우스");
        when(resultSet.getLong("age")).thenReturn(2L, 1L);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);

        // when
        String sql = "select * from person";
        List<TestDomain> domains = jdbcTemplate.query(sql, getRowMapper());

        // then
        assertThat(domains).hasSize(2)
                .extracting(TestDomain::getName, TestDomain::getAge)
                .containsExactly(tuple("망쵸", 2L), tuple("제우스", 1L));
    }

    @DisplayName("query 메서드 실행 시 전달된 인자로 조회한 객체들을 rowMapper로 매핑해서 반환한다.")
    @Test
    void queryTest1() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("망쵸");
        when(resultSet.getLong("age")).thenReturn(2L);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        ParameterMetaData mockedParameterMetaData = mock(ParameterMetaData.class);
        when(mockedParameterMetaData.getParameterCount()).thenReturn(1);
        when(mockedPreparedStatement.getParameterMetaData()).thenReturn(mockedParameterMetaData);

        // when
        String sql = "select * from person where age = ?";
        List<TestDomain> domains = jdbcTemplate.query(sql, getRowMapper(), 2L);

        // then
        assertThat(domains).hasSize(1)
                .extracting(TestDomain::getName, TestDomain::getAge)
                .containsExactly(tuple("망쵸", 2L));
    }

    @DisplayName("queryForObject 메서드 실행 시 조회한 객체를 Object에 매핑해서 반환한다.")
    @Test
    void queryForObjectTest() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("name")).thenReturn("망쵸");
        when(resultSet.getObject("age")).thenReturn(2L);
        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
        ParameterMetaData mockedParameterMetaData = mock(ParameterMetaData.class);
        when(mockedParameterMetaData.getParameterCount()).thenReturn(1);
        when(mockedPreparedStatement.getParameterMetaData()).thenReturn(mockedParameterMetaData);

        // when
        String sql = "select * from person where age = ?";
        TestDomain domain = jdbcTemplate.queryForObject(sql, TestDomain.class, 2L);

        // then
        assertAll(
                () -> assertThat(domain.getName()).isEqualTo("망쵸"),
                () -> assertThat(domain.getAge()).isEqualTo(2L)
        );
    }

    private RowMapper<TestDomain> getRowMapper() {
        return (rs, rowNum) -> new TestDomain(
                rs.getString("name"),
                rs.getLong("age"));
    }
}
