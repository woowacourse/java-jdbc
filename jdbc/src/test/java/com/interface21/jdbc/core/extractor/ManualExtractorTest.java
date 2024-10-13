package com.interface21.jdbc.core.extractor;

import com.interface21.jdbc.core.JdbcTemplateTest;
import com.interface21.jdbc.mapper.User;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ManualExtractorTest extends JdbcTemplateTest {


    @DisplayName("찾는 데이터가 없으면 찾지못한다.")
    @Test
    void test() throws Throwable {
        ResultSetExtractor<User> resultSetExtractor = new ManualExtractor<>(
                resultSet, rs -> new User(rs.getString(1), rs.getString(2)));

        List<User> result = resultSetExtractor.extract();
        Assertions.assertThat(result).isEmpty();
    }

}
