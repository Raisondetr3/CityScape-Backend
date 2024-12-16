package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbTransactionResource implements TwoPhaseCommitResource {

    private final PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;

    @Override
    public void prepare() {
        log.info("DB: Starting prepare phase. Opening new transaction...");
        this.transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        log.info("DB: Transaction opened.");
    }

    @Override
    public void commit() {
        log.info("DB: Committing transaction...");
        transactionManager.commit(transactionStatus);
        log.info("DB: Transaction committed successfully.");
    }

    @Override
    public void rollback() {
        log.info("DB: Rolling back transaction...");
        if (transactionStatus != null && !transactionStatus.isCompleted()) {
            transactionManager.rollback(transactionStatus);
            log.info("DB: Transaction rolled back successfully.");
        } else {
            log.info("DB: TransactionStatus is null or already completed, no rollback performed.");
        }
    }
}


