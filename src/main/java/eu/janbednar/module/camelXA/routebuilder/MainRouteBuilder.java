package eu.janbednar.module.camelXA.routebuilder;

import eu.janbednar.module.camelXA.processor.InsertRecordProcessor;
import eu.janbednar.module.camelXA.processor.ThrowProcessor;
import eu.janbednar.module.camelXA.transaction.CdiTransactionManager;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Route;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spi.RoutePolicy;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.camel.support.RoutePolicySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

@Startup
@ApplicationScoped
public class MainRouteBuilder extends SpringRouteBuilder {

    // Custom Spring propagations
    private static String REQUIRED = "PROPAGATION_REQUIRED_CUSTOM";
    private static String REQUIRES_NEW = "PROPAGATION_REQUIRES_NEW_CUSTOM";

    // Legacy Spring propagations from camel-cdi dependency
    //private static String REQUIRED = "PROPAGATION_REQUIRED";
    //private static String REQUIRES_NEW = "PROPAGATION_REQUIRES_NEW";

    @Inject
    private CdiTransactionManager transactionManager;

    @Inject
    InsertRecordProcessor insertRecordProcessor;

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    static Logger log = LoggerFactory.getLogger(MainRouteBuilder.class);

    private static Predicate HAS_EXCEPTION = e -> {
        boolean result= e.getProperty(Exchange.EXCEPTION_CAUGHT) != null;
        log.info("HAS_EXCEPTION: "+result);
        return result;
    };

    public void configure() throws Exception {
        getContext().addComponent("jms", JmsComponent.jmsComponentTransacted(connectionFactory,transactionManager));

        onException(Exception.class)
                .log(LoggingLevel.INFO, getClass().getCanonicalName(), "About to rollback")
                .markRollbackOnlyLast();// Only last transaction will be rolled back


        from("direct:hello")
                .transacted(REQUIRED)
                //.throwException(new Exception())
                .to("log:hi");

        from("direct:jmsMessage")
                .to("jms:queue:Test?exchangePattern=InOnly");

        from("jms:queue:DLQ?exchangePattern=InOnly")
                .to("seda:dlq");

        from("jms:queue:Test?exchangePattern=InOnly")
                .autoStartup(false)
                .routeId("input")
                .routePolicy(topLevelRoutePolicy())
                .transacted(REQUIRED)
                .to("direct:requiresNewAndRollback") // should be rolled back
                .to("direct:requiresNewWithoutRollback")//should be commited for every redelivery
                .to("jms:queue:TestOut")
                .to("log:done")
                //.choice().when(HAS_EXCEPTION).rollback().end()
        ;

        from("direct:requiresNewAndRollback")
                .transacted(REQUIRES_NEW)
                .process(insertRecordProcessor)
                .to("log:beforeRollback")
                .process(new ThrowProcessor(IllegalStateException.class))
                //.process(new KillProcessor())
                .to("log:sholdNotGoHere");

        from("direct:requiresNewWithoutRollback")
                .transacted(REQUIRES_NEW)
                .process(insertRecordProcessor)
                .to("log:shouldBeOk");
    }

    RoutePolicy topLevelRoutePolicy(){
        return new RoutePolicySupport() {
            @Override
            public void onExchangeDone(Route route, Exchange exchange) {
                if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null){
                    exchange.setProperty(Exchange.ROLLBACK_ONLY, true);
                    log.error("Rollback of JTA transaction", exchange.getProperty(Exchange.EXCEPTION_CAUGHT));
                }
                super.onExchangeDone(route, exchange);
            }
        };
    }
/*
    @PostConstruct
    public void start(){
        try {
            getContext().start();
        } catch (Exception e){
            throw new RuntimeException(e);
        }

    }*/
}
