package eu.janbednar.module.camelXA.transaction;

import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This is just a simple named bean that is used by camel to configure transactions.
 * It should be used in cases where  it is required to propagate transactions through the route.
 * Camel will propagate transactions through the route(beans, directs ...)until it reaches
 * an endpoint that is either not transactional(seda, InOnly jms ...),
 * or it reaches the end of the route.
 */
@Named("PROPAGATION_REQUIRED_CUSTOM")
public class CdiRequiredPolicy extends SpringTransactionPolicy {

    @Inject
    public CdiRequiredPolicy(CdiTransactionManager cdiTransactionManager) {
        super(new TransactionTemplate(cdiTransactionManager,
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED)));
    }
}