package ioc.decoupled;

import java.util.Properties;

public final class ExchangeRateSupportFactory {
    private static final ExchangeRateSupportFactory instance;
    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRateRenderer exchangeRateRenderer;

    static {
        instance = new ExchangeRateSupportFactory();
    }

    private ExchangeRateSupportFactory() {
        Properties properties = new Properties();
        try {
            final var resourceStream = this.getClass().getResourceAsStream("/exchange-rate.properties");
            properties.load(resourceStream);

            final var providerClass = properties.getProperty("provider.class");
            final var rendererClass = properties.getProperty("renderer.class");

            this.exchangeRateProvider = (ExchangeRateProvider) Class.forName(providerClass).getDeclaredConstructor().newInstance();
            this.exchangeRateRenderer = (ExchangeRateRenderer) Class.forName(rendererClass).getDeclaredConstructor().newInstance();

            exchangeRateRenderer.setExchangeRateProvider(exchangeRateProvider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ExchangeRateSupportFactory getInstance() {
        return instance;
    }

    public ExchangeRateProvider getExchangeRateProvider() {
        return exchangeRateProvider;
    }

    public ExchangeRateRenderer getExchangeRateRenderer() {
        return exchangeRateRenderer;
    }
}
