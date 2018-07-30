package eu.janbednar.module.ejbTest.beans;

import org.slf4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Stateless
@LocalBean
@Named
public abstract class AbstractBean implements Processor {



    @Inject
    SomeDummyCdiAndStatelessBean someDummyCdiAndStatelessBean;

    @Inject
    SomeDummyCdiBean someDummyCdiBean;

    //@Override
    public void process(Object exchange) throws Exception {
        if (someDummyCdiAndStatelessBean==null) throw new NullPointerException("someDummyCdiAndStatelessBean");
        if (someDummyCdiBean==null) throw new NullPointerException("someDummyCdiBean");
        //if (logger==null) throw new NullPointerException("logger");
        operation(exchange);
    }

    public abstract void operation(Object exchange) throws Exception;
}
