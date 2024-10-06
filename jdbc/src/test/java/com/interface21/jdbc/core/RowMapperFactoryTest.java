package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.support.TestDomain;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class RowMapperFactoryTest {

    @Test
    void getRowMapperTest() throws SQLException {
        // given
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(any())).thenReturn("망쵸", 2L);

        // when
        RowMapper<TestDomain> rowMapper = RowMapperFactory.getRowMapper(TestDomain.class);
        TestDomain mappedDomain = rowMapper.mapRow(resultSet);

        // then
        assertAll(
                () -> assertThat(mappedDomain.getName()).isEqualTo("망쵸"),
                () -> assertThat(mappedDomain.getAge()).isEqualTo(2L)
        );
    }
}
