package eu.janbednar.module.ejbTest.beans;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public abstract class AbstractBean implements Processor {

    @Inject
    SomeDummyCdiAndStatelessBean someDummyCdiAndStatelessBean;

    @Inject
    SomeDummyCdiBean someDummyCdiBean;

    //@Override
    public void process(Object exchange) throws Exception {
        if (someDummyCdiAndStatelessBean==null) throw new NullPointerException("someDummyCdiAndStatelessBean");
        if (someDummyCdiBean==null) throw new NullPointerException("someDummyCdiBean");
        operation(exchange);
    }

    public abstract void operation(Object exchange) throws Exception;
}
