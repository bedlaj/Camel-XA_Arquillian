package eu.janbednar.module.ejbTest.beans;

import javax.inject.Named;

@Named
public class AnotherDummyCdiBean implements Processor {
    @Override
    public void process(Object exchange) throws Exception {

    }
}
