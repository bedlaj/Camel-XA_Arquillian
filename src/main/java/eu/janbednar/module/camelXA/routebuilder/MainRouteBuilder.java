package eu.janbednar.module.camelXA.routebuilder;

import eu.janbednar.module.camelXA.processor.InsertRecordProcessor;
import eu.janbednar.module.camelXA.processor.SendProcessor;
import eu.janbednar.module.camelXA.processor.SetHeaderOnIterationRouter;
import eu.janbednar.module.camelXA.processor.ThrowProcessor;
import eu.janbednar.module.camelXA.transaction.CdiRequiredPolicy;
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

    @Inject
    private SendProcessor sendProcessor;

    @Inject
    private CdiRequiredPolicy cdiRequiredPolicy;

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
               // .removeProperty(Exchange.TRY_ROUTE_BLOCK)
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
                .to("direct:router")
                .to("direct:requiresNewWithoutRollback")//should be commited for every redelivery
                //.to("direct:requiresNewAndRollback") // should be rolled back

                .process(sendProcessor) //This sends JMS message to TestOut queue using ProducerTemplate. Should be rolled back from queue
                .to("direct:all")

                .to("log:done")
        ;

        from("direct:router")
                .transacted(REQUIRES_NEW)
                .process(e-> System.out.println("inRouter"))
                //.setProperty(Exchange.TRY_ROUTE_BLOCK, constant(true))
                .process(new SetHeaderOnIterationRouter(3));
                //.dynamicRouter().method(new SetHeaderOnIterationRouter(3), "route");

        from("direct:inRouter")
                //.transacted(REQUIRED)
                .process(insertRecordProcessor)
                .choice().when(header("throw").isEqualTo(true)).process(new ThrowProcessor(RuntimeException.class))
                .otherwise().end();

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

        //All this should be rolled back
        from("direct:all")
                .transacted(REQUIRES_NEW)
                .process(insertRecordProcessor)
                .to("direct:this");

        from("direct:this")
                //.transacted(REQUIRED)
                .process(insertRecordProcessor)
                .to("direct:should");

        from("direct:should")
                //.transacted(REQUIRED)
                .process(insertRecordProcessor)
                .to("direct:be");

        from("direct:be")
                //.transacted(REQUIRED)
                .process(insertRecordProcessor)
                .to("direct:rolled");

        from("direct:rolled")
                //.transacted(REQUIRED)
                .process(insertRecordProcessor)
                .to("direct:back");

        from("direct:back")
                //.transacted(REQUIRED)
                .to("direct:butThisShouldNot")
                .process(new ThrowProcessor(IllegalStateException.class)).to("log:shouldNotBeThere");


        from("direct:butThisShouldNot")
                .transacted(REQUIRES_NEW)
                .process(insertRecordProcessor)
                .to("log:butThisShouldNot");
    }

    private static RoutePolicy topLevelRoutePolicy(){
        return new RoutePolicySupport() {
            @Override
            public void onExchangeDone(Route route, Exchange exchange) {
                if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) != null && exchange.isTransacted()){
                    //exchange.setProperty(Exchange.ROLLBACK_ONLY, true);
                    //exchange.setException(new RollbackExchangeException(exchange, exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class)));
                    log.error("Rollback of JTA transaction", exchange.getProperty(Exchange.EXCEPTION_CAUGHT));
                    exchange.setException(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class));
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
