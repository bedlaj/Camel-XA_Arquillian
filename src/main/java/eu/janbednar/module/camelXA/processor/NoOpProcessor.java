package eu.janbednar.module.camelXA.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

@Named
public class NoOpProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        LoggerFactory.getLogger(getClass()).info("Doing nothing with body "+exchange.getIn().getBody());
    }
}
