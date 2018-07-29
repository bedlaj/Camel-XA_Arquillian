package eu.janbednar.module.camelXA.transaction;

import org.apache.camel.Processor;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.spring.spi.TransactionErrorHandler;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Want to start new transaction in the middle of the route?
 * Well congratulations! You found correct bean to feed to the camel.
 * Beware however. Using this doesn't make the camel commit the previously started transaction.
 * This is just to tell camel that it might not be needed to rollback the whole transaction
 * and just rollback to the place where this is used.
 */
@Named("PROPAGATION_REQUIRES_NEW_CUSTOM")
public class CdiRequiresNewPolicy extends SpringTransactionPolicy {

    @Inject
    public CdiRequiresNewPolicy(CdiTransactionManager cdiTransactionManager) {
        super(new TransactionTemplate(cdiTransactionManager,
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW)));
    }


}
