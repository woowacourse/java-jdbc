package ioc.decoupled;

import java.util.Scanner;

public class StandardInputExchangeRateProvider implements ExchangeRateProvider {

    private static final Scanner scanner = new Scanner(System.in);

    @Override
    public double getExchangeRate() {
//        return scanner.nextDouble();
        return 1300;
    }
}
