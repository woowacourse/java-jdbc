package practice;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class practice0 {

    @Test
    void stage1() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
            }
        });

        thread.start();
    }

    @Test
    void functional() {
        Predicate<Integer> isEven = i -> i % 2 == 0;
        boolean test = isEven.test(4);
        System.out.println(test);

        Function<String, Integer> length = String::length;
        System.out.println(length.apply("1234"));

        Consumer<String> consume = System.out::println;
        consume.accept("1234");

        Supplier<String> supplier = () -> {return "1234";};
        System.out.println(supplier.get());

        BiFunction<String, Integer, Integer> biFunction = (s,i) -> s.length() + i;
        System.out.println(biFunction.apply("1234", 4));

        Calculator calculator = new Calculator() {
            @Override
            public int calculate(int a, int b) {
                return a + b;
            }
        };

        System.out.println(calculator.calculate(10, 20));

        PostService postService = new PostService();

        List<Long> tests = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);
        List<Long> longs = postService.filterLong(tests, i -> i % 2 == 0);
        System.out.println(longs);
    }
}

@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

class PostService {
    public List<Long> filterLong(List<Long> longs, Predicate<Long> condition) {
        return longs.stream()
                .filter(condition)
                .toList();
    }
}
