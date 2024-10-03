package ioc.decoupled;

public class StandardOutputExchangeRateRenderer implements ExchangeRateRenderer {
    private ExchangeRateProvider provider;

    @Override
    public void render() {
        System.out.printf("1달러 환율: %.2f원%n", provider.getExchangeRate());
    }

    @Override
    public void setExchangeRateProvider(ExchangeRateProvider provider) {
        this.provider = provider;
    }

    @Override
    public ExchangeRateProvider getExchangeRateProvider() {
        return provider;
    }
}
