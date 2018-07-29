package eu.janbednar.module.camelXA.processor;

import eu.janbednar.module.domain.dao.TestDao;
import eu.janbednar.module.domain.entity.TestEntity;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class InsertRecordProcessor implements Processor {
    @Inject
    TestDao dao;

    @Override
    public void process(Exchange exchange) throws Exception {
        TestEntity entity = new TestEntity();
        entity.setValue(exchange.getIn().getBody(String.class));
        entity.setCorrelationId(exchange.getIn().getHeader("JMSCorrelationID", String.class));
        entity.setFromEndpoint(exchange.getFromEndpoint().getEndpointUri());
        entity.setRouteId(entity.getRouteId());
        dao.save(entity);
    }
}
