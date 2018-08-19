package eu.janbednar.module.camelXA.routebuilder;

import eu.janbednar.module.camelXA.processor.SendProcessor;
import eu.janbednar.module.camelXA.processor.UpdateRecordProcessor;
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
public class MainRouteBuilderRaceCondition extends SpringRouteBuilder {

    // Custom Spring propagations
    private static String REQUIRED = "PROPAGATION_REQUIRED_CUSTOM";
    private static String REQUIRES_NEW = "PROPAGATION_REQUIRES_NEW_CUSTOM";

    // Legacy Spring propagations from camel-cdi dependency
    //private static String REQUIRED = "PROPAGATION_REQUIRED";
    //private static String REQUIRES_NEW = "PROPAGATION_REQUIRES_NEW";

    @Inject
    private CdiTransactionManager transactionManager;

    @Inject
    UpdateRecordProcessor updateRecordProcessor;

    @Resource(mappedName = "java:/JmsXA")
    private ConnectionFactory connectionFactory;

    @Inject
    private SendProcessor sendProcessor;

    static Logger log = LoggerFactory.getLogger(MainRouteBuilderRaceCondition.class);

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

        from("jms:queue:Test")
                .transacted(REQUIRED)
                .process(updateRecordProcessor)
                .to("jms:queue:Test");


    }

    private static RoutePolicy topLevelRoutePolicy(){
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
