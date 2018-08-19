package eu.janbednar.module.camelXA.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.slf4j.LoggerFactory;

public abstract class DynamicRouterCustom implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate();

        String to;
        Exchange newExchange;
        while ((to = route(exchange)) != null){
            newExchange = producerTemplate.send(to, exchange);
            if (newExchange.getException() != null){
                LoggerFactory.getLogger(getClass()).warn("End routing",newExchange.getException());
                exchange.setException(newExchange.getException());
                return;
            }
        }
    }

    public abstract String route(Exchange exchange);
}
