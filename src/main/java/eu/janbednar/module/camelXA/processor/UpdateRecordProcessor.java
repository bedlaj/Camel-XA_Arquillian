package eu.janbednar.module.camelXA.processor;

import eu.janbednar.module.domain.dao.TestDao;
import eu.janbednar.module.domain.entity.TestEntityUpdate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class UpdateRecordProcessor implements Processor {
    @Inject
    TestDao dao;

    @Override
    public void process(Exchange exchange) throws Exception {
        TestEntityUpdate e = dao.find(TestEntityUpdate.class, exchange.getIn().getBody(Long.class));
        if (e == null){
            e = new TestEntityUpdate();
            e.setId(exchange.getIn().getBody(Long.class));
            e.setValue(0L);
        } else {
            e.setValue(e.getValue()+1);
        }
        dao.merge(e);
        if (!exchange.getIn().getHeader("val", 0L, Long.class).equals(e.getValue())){
            throw new IllegalStateException("Race condition: "+exchange.getIn().getHeader("val"));
        }

        exchange.getIn().setHeader("val", exchange.getIn().getHeader("val", 0L, Long.class) + 1);
    }
}
