package com.revolut.money.transfer.service;

import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.Transaction;

import java.util.Collection;

public interface TransactionsService {

    Collection<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    Transaction createTransaction(Transaction transaction) throws ObjectModificationException;

    void executeTransactions();
}
