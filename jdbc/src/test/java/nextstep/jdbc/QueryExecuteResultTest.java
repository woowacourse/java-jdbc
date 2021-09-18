package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueryExecuteResultTest {

    @DisplayName("응답 결과가 -1 일 때 isSuccess가 False를 반환하는지 확인")
    @Test
    void isSuccessTest() {
        //given
        QueryExecuteResult failResult = new QueryExecuteResult(-1);
        //when
        //then
        assertThat(failResult.isSuccess()).isFalse();
    }

    @DisplayName("영향받은 row 수가 잘 나오는지 확인")
    @Test
    void effectedRowTest() {
        //given
        QueryExecuteResult queryExecuteResult = new QueryExecuteResult(10);
        //when
        //then
        assertThat(queryExecuteResult.effectedRow()).isEqualTo(10);
    }
}