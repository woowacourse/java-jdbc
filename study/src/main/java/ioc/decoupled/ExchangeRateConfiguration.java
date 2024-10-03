package ioc.decoupled;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@ComponentScan(basePackages = {"com.example.ioc.decoupled"})
//@ImportResource(locations = {"classpath:app-context-xml.xml"})
@Configuration
public class ExchangeRateConfiguration {

    @Bean
    public ExchangeRateProvider exchangeRateProvider() {
        return new DaumExchangeRateProvider();
    }

    @Bean
    public ExchangeRateRenderer exchangeRateRenderer() {
        final var renderer = new StandardOutputExchangeRateRenderer();
        renderer.setExchangeRateProvider(exchangeRateProvider());
        return renderer;
    }
}
