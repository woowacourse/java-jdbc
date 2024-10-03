package ioc.decoupled;

public interface ExchangeRateRenderer {
    void render();

    void setExchangeRateProvider(ExchangeRateProvider provider);

    ExchangeRateProvider getExchangeRateProvider();
}
