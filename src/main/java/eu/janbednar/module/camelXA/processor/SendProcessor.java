package eu.janbednar.module.camelXA.processor;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;

import javax.inject.Named;

@Named
public class SendProcessor implements Processor {

    @EndpointInject
    ProducerTemplate producerTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {
        producerTemplate.sendBodyAndHeader("jms:queue:TestOut",exchange.getIn().getBody(),
                "JMSCorrelationID", exchange.getIn().getHeader("JMSCorrelationID"));
    }
}
