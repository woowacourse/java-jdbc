package com.interface21.jdbc.core.extractor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.CannotReleaseJdbcResourceException;
import com.interface21.jdbc.mapper.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResultSetExtractorTest {

    @DisplayName("자원을 정상적으로 반환하지 못하면 특정 예외를 발생시킨다.")
    @Test
    void extract() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ManualExtractor<User> extractor = new ManualExtractor<>(resultSet, rs -> new User());
        doThrow(new SQLException()).when(resultSet).close();

        Assertions.assertThatThrownBy(extractor::close)
                .isInstanceOf(CannotReleaseJdbcResourceException.class);
    }

    @Test
    void extractOne() {
    }

    @Test
    void close() {
    }
}
