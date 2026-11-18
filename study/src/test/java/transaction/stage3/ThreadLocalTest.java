package transaction.stage3;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadLocalTest {

    @Test
    void eachThreadHasItsOwnValue() throws Exception {
        final var local = new ThreadLocal<String>();
        local.set("connection-A");
        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                assertThat(local.get()).isNull();
                try {
                    local.set("connection-B");
                    assertThat(local.get()).isEqualTo("connection-B");
                } finally {
                    local.remove();
                }
                assertThat(local.get()).isNull();
            }).get(5, TimeUnit.SECONDS);

            assertThat(local.get()).isEqualTo("connection-A");
        } finally {
            local.remove();
        }
        assertThat(local.get()).isNull();
    }

    @Test
    void reusedThreadRetainsValueUntilRemoved() throws Exception {
        final var local = new ThreadLocal<String>();
        try (final var executor = Executors.newSingleThreadExecutor()) {
            // 요청 1이 정리를 빠뜨린 상황. 같은 작업 스레드를 다음 요청에서도 사용한다.
            executor.submit(() -> local.set("previous-request"))
                    .get(5, TimeUnit.SECONDS);

            executor.submit(() -> {
                try {
                    assertThat(local.get()).isEqualTo("previous-request");
                } finally {
                    local.remove();
                }
            }).get(5, TimeUnit.SECONDS);

            assertThat(executor.submit(local::get).get(5, TimeUnit.SECONDS)).isNull();
        }
    }
}
