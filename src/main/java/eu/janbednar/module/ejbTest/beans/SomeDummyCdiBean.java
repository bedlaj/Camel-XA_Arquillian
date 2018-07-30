package eu.janbednar.module.ejbTest.beans;

import javax.inject.Named;

@Named
public class SomeDummyCdiBean implements Processor {
    @Override
    public void process(Object exchange) throws Exception {

    }
}
