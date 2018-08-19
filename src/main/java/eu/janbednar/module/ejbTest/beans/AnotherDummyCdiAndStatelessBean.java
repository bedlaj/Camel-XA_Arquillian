package eu.janbednar.module.ejbTest.beans;


import javax.ejb.Stateless;
import javax.inject.Named;

@Stateless
@Named
public class AnotherDummyCdiAndStatelessBean implements Processor, IAnotherDummyCdiAndStatelessBean {
    @Override
    public void process(Object exchange) throws Exception {

    }
}
