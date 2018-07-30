package eu.janbednar.module.ejbTest.beans;


import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Stateless
//@LocalBean
@Named
public class SomeDummyCdiAndStatelessBean extends AbstractBean{

    @Inject
    SomeDummyCdiBean someDummyCdiBean;

/*    //@Override
    public void process(Object exchange) throws Exception {
        if (someDummyCdiBean==null) throw new NullPointerException("someDummyCdiBean");
    }*/

    @Override
    public void operation(Object exchange) throws Exception {
        if (someDummyCdiBean==null) throw new NullPointerException("someDummyCdiBean");
    }
}
