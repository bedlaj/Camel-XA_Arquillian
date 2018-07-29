package eu.janbednar.module.camelXA.transaction;

import org.springframework.transaction.jta.JtaTransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

@Named("transactionManager")
public class CdiTransactionManager extends JtaTransactionManager {

    /**
     *
     */
    private static final long serialVersionUID = 3094120987654323457L;

    @Resource(mappedName = "java:/TransactionManager")
    private transient TransactionManager transactionManager;

    @Resource
    private transient UserTransaction userTransaction;

    @PostConstruct
    public void initTransactionManager() {
        setTransactionManager(transactionManager);
        setUserTransaction(userTransaction);
    }
}