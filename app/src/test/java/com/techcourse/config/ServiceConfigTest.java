package com.techcourse.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.service.UserService;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;

class ServiceConfigTest {

    @Test
    @DisplayName("userService는 트랜잭션 프록시를 돌려준다")
    void returnsProxy() {
        assertThat(AopUtils.isAopProxy(ServiceConfig.userService())).isTrue();
    }

    @Test
    @DisplayName("여러 스레드가 동시에 호출해도 같은 인스턴스를 돌려준다")
    void returnsSameInstanceConcurrently() throws Exception {
        final int threadCount = 16;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch start = new CountDownLatch(1);
        try {
            final List<Callable<UserService>> tasks = IntStream.range(0, threadCount)
                    .<Callable<UserService>>mapToObj(i -> () -> {
                        start.await();
                        return ServiceConfig.userService();
                    })
                    .toList();
            final List<Future<UserService>> futures = tasks.stream().map(executor::submit).toList();
            start.countDown();

            final UserService first = futures.get(0).get();
            for (final Future<UserService> future : futures) {
                assertThat(future.get()).isSameAs(first);
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
