package eu.janbednar.module.camelXA.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class KillProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        System.exit(0);
    }
}
