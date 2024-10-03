package ioc;

import ioc.decoupled.ExchangeRateConfiguration;
import ioc.decoupled.ExchangeRateRenderer;
import ioc.decoupled.ExchangeRateSupportFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class IocApplication {
    public static void main(String[] args) {
        myIocContainer();
        xmlApplicationContext();
        annotationApplicationContext();
    }

    private static void myIocContainer() {
        final ExchangeRateSupportFactory factory = ExchangeRateSupportFactory.getInstance();
        final ExchangeRateRenderer renderer = factory.getExchangeRateRenderer();
        renderer.render();
    }

    private static void xmlApplicationContext() {
        final ApplicationContext context = new ClassPathXmlApplicationContext("exchange-rate-context.xml");
        final ExchangeRateRenderer renderer = context.getBean("exchangeRateRenderer", ExchangeRateRenderer.class);
        renderer.render();
    }

    private static void annotationApplicationContext() {
        final ApplicationContext context = new AnnotationConfigApplicationContext(ExchangeRateConfiguration.class);
        final ExchangeRateRenderer renderer = context.getBean("exchangeRateRenderer", ExchangeRateRenderer.class);
        renderer.render();
    }
}
