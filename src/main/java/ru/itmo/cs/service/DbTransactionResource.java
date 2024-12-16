package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
@RequiredArgsConstructor
public class DbTransactionResource implements TwoPhaseCommitResource {

    private final PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;

    @Override
    public void prepare() {
        this.transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
    }

    @Override
    public void commit() {
        transactionManager.commit(transactionStatus);
    }

    @Override
    public void rollback() {
        if (transactionStatus != null && !transactionStatus.isCompleted()) {
            transactionManager.rollback(transactionStatus);
        }
    }
}

