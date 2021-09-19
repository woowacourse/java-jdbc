package reflection;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Junit3TestRunner {

    private ByteArrayOutputStream byteArrayOutputStream;

    @BeforeEach
    void setUp() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(byteArrayOutputStream));
    }

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;
        Junit3Test junit3Test = clazz.getConstructor().newInstance();

        Arrays.stream(clazz.getDeclaredMethods())
            .filter(method -> method.getName().startsWith("test"))
            .forEach(method -> {
                try {
                    method.invoke(junit3Test);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });

        assertThat("Running Test1" + System.lineSeparator() + "Running Test2" + System.lineSeparator()).isEqualTo(byteArrayOutputStream.toString());
    }
}
