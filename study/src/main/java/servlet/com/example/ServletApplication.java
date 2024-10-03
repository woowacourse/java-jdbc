package servlet.com.example;

public class ServletApplication {

    public static void main(String[] args) {
        final var tomcatStarter = new TomcatStarter();
        tomcatStarter.start();
        tomcatStarter.await();
    }
}
