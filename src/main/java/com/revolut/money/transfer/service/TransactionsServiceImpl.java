package com.revolut.money.transfer.service;

import com.revolut.money.transfer.core.AbstractService;
import com.revolut.money.transfer.core.Services;
import com.revolut.money.transfer.dao.TransactionDao;
import com.revolut.money.transfer.exceptions.ExceptionType;
import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.Transaction;
import com.revolut.money.transfer.model.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Right now the proxy service under the {@link TransactionDao}. Should be used to abstract the presentation layer
 * from the persistence layer.
 * <p>
 * Additionally it schedule the transaction execution service.
 * <p>
 * TODO: make TransactionDao as an interface and pass it into the constructor. Use DI.
 */
public class TransactionsServiceImpl extends AbstractService implements TransactionsService {
    private static final Logger log = LoggerFactory.getLogger(TransactionsServiceImpl.class);

    private static TransactionsService ts;
    private TransactionDao transactionDao;
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    /**
     * Constructor made just for testing purpose
     */
    TransactionsServiceImpl(Services services) {
        super(services);
        this.transactionDao = TransactionDao.getInstance(services.getMoneyExchangeService());
        executorService.scheduleAtFixedRate(() ->
                        ts.executeTransactions(),
                0, 5, TimeUnit.SECONDS);
        log.info("Transaction Executor planned");
    }

    public static TransactionsService getInstance(Services services) {
        if (ts == null) {
            synchronized (TransactionsServiceImpl.class) {
                if (ts == null) {
                    ts = new TransactionsServiceImpl(services);
                }
            }
        }
        return ts;
    }

    public Collection<Transaction> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    private Collection<Long> getAllTransactionIdsByStatus(TransactionStatus transactionStatus) {
        return transactionDao.getAllTransactionIdsByStatus(transactionStatus);
    }

    public Transaction getTransactionById(Long id) {
        return transactionDao.getTransactionById(id);
    }

    /**
     * Make it possible to create money transfer from one account to another.
     * The result of execution is created transaction with actual status. Usually it is "IN PROGRESS"
     * <p>
     * The transaction <code>fromBankAccount</code> and <code>toBankAccount</code> may have not specified any
     * fields except id
     *
     * @return transaction object with the actual ID
     */
    public Transaction createTransaction(Transaction transaction) throws ObjectModificationException {
        if (transaction.getFromBankAccountId() == null || transaction.getToBankAccountId() == null) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED,
                    "The transaction has not provided from Bank Account or to Bank Account values");
        }
        if (transaction.getFromBankAccountId().equals(transaction.getToBankAccountId())) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED,
                    "The sender and recipient should not be same");
        }
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED,
                    "The amount should be more than 0");
        }

        return transactionDao.createTransaction(transaction);
    }

    /**
     * Here we are taking all  transactions in Created State and executing them.
     * After execution the transaction status will be changed
     */
    public void executeTransactions() {
        log.info("Starting of Transaction executor");
        Collection<Long> plannedTransactionIds = getAllTransactionIdsByStatus(TransactionStatus.CREATED);

        for (Long transactionId : plannedTransactionIds) {
            try {
                transactionDao.executeTransaction(transactionId);
            } catch (ObjectModificationException e) {
                log.error("Could not execute transaction with id %d", transactionId, e);
            }
        }
        log.info("Transaction executor ended");
    }
}
