package eu.janbednar.module.ejbTest.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class Imports {

    static Logger log = LoggerFactory.getLogger(Imports.class);

    @Produces
    public Logger produceLogger(InjectionPoint injectionPoint){
        log.info("injection of Logger to "+injectionPoint);
        return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());
    }
}
