package com.truong.restapi.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
	
    @Override
    protected Object determineCurrentLookupKey() {
    	System.out.println(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "read" : "write";
    }
}