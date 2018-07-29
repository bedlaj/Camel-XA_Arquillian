package eu.janbednar.module.camelXA.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import javax.inject.Named;

public class ThrowProcessor implements Processor {

    Class<? extends Throwable> exceptionClass;

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwException(Throwable exception) throws T
    {
        throw (T) exception;
    }
    public ThrowProcessor(Class<? extends Throwable> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        throwException(exceptionClass.newInstance());
    }
}
