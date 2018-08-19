package eu.janbednar.module.camelXA.processor;

import org.apache.camel.Exchange;

public class SetHeaderOnIterationRouter extends DynamicRouterCustom {

    int th = 0;
    int i = 0;

    public SetHeaderOnIterationRouter(int th) {
        this.th = th;
    }

    public String route(Exchange exchange){
        if (i%th==0){
            exchange.getIn().setHeader("throw", true);
            System.out.println("throw it");
        }
        i++;
        System.out.println("to direct:inRoute");
        //exchange.setProperty(Exchange.TRY_ROUTE_BLOCK, true);
        return "direct:inRouter";
    }
}
