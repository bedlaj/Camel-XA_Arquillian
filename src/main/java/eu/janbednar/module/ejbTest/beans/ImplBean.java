package eu.janbednar.module.ejbTest.beans;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Stateless
public class ImplBean extends AbstractBean {


    @Inject
    AnotherDummyCdiBean anotherDummyCdiBean;

    @Inject
    IAnotherDummyCdiAndStatelessBean anotherDummyCdiAndStatelessBean;

    @Override
    public void operation(Object exchange) throws Exception{
        if (someDummyCdiAndStatelessBean==null) throw new NullPointerException("someDummyCdiAndStatelessBean");
        if (someDummyCdiBean==null) throw new NullPointerException("someDummyCdiBean");
        if (anotherDummyCdiAndStatelessBean ==null) throw new NullPointerException("anotherDummyCdiAndStatelessBean");
        if (anotherDummyCdiBean==null) throw new NullPointerException("anotherDummyCdiBean");
        someDummyCdiAndStatelessBean.process(exchange);
    }
}
