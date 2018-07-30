package eu.janbednar.module.ejbTest.beans;


import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Named;

@Stateless
@LocalBean
@Named
public class AnotherDummyCdiAndStatelessBean implements Processor {
    @Override
    public void process(Object exchange) throws Exception {

    }
}
