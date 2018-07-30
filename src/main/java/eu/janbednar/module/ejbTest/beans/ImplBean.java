package eu.janbednar.module.ejbTest.beans;

import org.slf4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Stateless
//@LocalBean
public class ImplBean extends AbstractBean implements Operation{

    @Inject
    public AnotherDummyCdiBean anotherDummyCdiBean;

    @Inject
    public AnotherDummyCdiAndStatelessBean anotherDummyCdiAndStatelessBean;

    @Inject
    Logger logger;

    public ImplBean() {
        //new Throwable().printStackTrace();
    }

    @Override
    public void operation(Object exchange) throws Exception{
        if (someDummyCdiAndStatelessBean==null) throw new NullPointerException("someDummyCdiAndStatelessBean");
        if (someDummyCdiBean==null) throw new NullPointerException("someDummyCdiBean");
        if (anotherDummyCdiAndStatelessBean ==null) throw new NullPointerException("anotherDummyCdiAndStatelessBean");
        if (anotherDummyCdiBean==null) throw new NullPointerException("anotherDummyCdiBean");
        if (logger==null) throw new NullPointerException("logger");
        someDummyCdiAndStatelessBean.process(exchange);
        System.out.println("Called "+this+" with "+exchange);
    }

    public AnotherDummyCdiBean getAnotherDummyCdiBean() {
        return anotherDummyCdiBean;
    }

    public void setAnotherDummyCdiBean(AnotherDummyCdiBean anotherDummyCdiBean) {
        this.anotherDummyCdiBean = anotherDummyCdiBean;
    }

    public AnotherDummyCdiAndStatelessBean getAnotherDummyCdiAndStatelessBean() {
        return anotherDummyCdiAndStatelessBean;
    }

    public void setAnotherDummyCdiAndStatelessBean(AnotherDummyCdiAndStatelessBean anotherDummyCdiAndStatelessBean) {
        this.anotherDummyCdiAndStatelessBean = anotherDummyCdiAndStatelessBean;
    }
}
