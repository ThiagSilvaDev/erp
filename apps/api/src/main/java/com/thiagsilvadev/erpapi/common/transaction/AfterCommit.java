package com.thiagsilvadev.erpapi.common.transaction;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Runs a side effect only after the surrounding transaction commits, or immediately when there is no
 * active transaction.
 *
 * <p>Use this instead of registering {@link TransactionSynchronization} by hand so that every
 * after-commit side effect behaves the same way: it never observes uncommitted state and it never
 * runs for rolled-back work.
 */
public final class AfterCommit {

    private AfterCommit() {}

    public static void run(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
