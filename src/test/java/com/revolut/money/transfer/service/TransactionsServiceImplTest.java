package com.revolut.money.transfer.service;

import com.revolut.money.transfer.core.ServiceFactory;
import com.revolut.money.transfer.dao.TransactionDao;
import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.Currency;
import com.revolut.money.transfer.model.Transaction;
import com.revolut.money.transfer.model.TransactionStatus;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.revolut.money.transfer.utils.Constants.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertArrayEquals;

/**
 * This Test is for transaction service unit test
 */
public class TransactionsServiceImplTest {
    private static final TransactionsService transactionService = ServiceFactory.createServices().getTransactionsService();


    @Test
    public void testAllTransactionsRetrieval() {
        TransactionDao transactionDao = mock(TransactionDao.class);
        TransactionsServiceImpl transactionsServiceImpl = (TransactionsServiceImpl) ServiceFactory.createServices().getTransactionsService();
        transactionsServiceImpl.setTransactionDao(transactionDao);

        Collection<Transaction> testList = Arrays.asList(
                new Transaction(
                        TATA_BANK_ACCOUNT_ID,
                        SIEMENS_BANK_ACCOUNT_ID,
                        BigDecimal.ZERO,
                        Currency.EUR),
                new Transaction(
                        SIEMENS_BANK_ACCOUNT_ID,
                        NAGARRO_BANK_ACCOUNT_ID,
                        BigDecimal.ZERO,
                        Currency.EUR)
        );

        when(transactionDao.getAllTransactions()).thenReturn(testList);

        Collection<Transaction> transactions = transactionsServiceImpl.getAllTransactions();

        assertNotNull(transactions);
        assertArrayEquals(testList.toArray(), transactions.toArray());
    }

    /**
     * //Test null from account
     *
     * @throws ObjectModificationException
     */
    @Test(expectedExceptions = ObjectModificationException.class)
    public void testCreateTransactionWithNullFrom() throws ObjectModificationException {
        transactionService.createTransaction(new Transaction(
                null, 2L, BigDecimal.TEN, Currency.INR
        ));
    }

    /**
     * Test null to account
     *
     * @throws ObjectModificationException
     */
    @Test(expectedExceptions = ObjectModificationException.class)
    public void testCreateTransactionWithNullTo() throws ObjectModificationException {
        transactionService.createTransaction(new Transaction(
                1L, null, BigDecimal.TEN, Currency.INR
        ));
    }

    /**
     * Test transaction creation with the same accounts
     *
     * @throws ObjectModificationException
     */
    @Test(expectedExceptions = ObjectModificationException.class)
    public void testCreateTransactionWithSameAccounts() throws ObjectModificationException {
        transactionService.createTransaction(new Transaction(
                TATA_BANK_ACCOUNT_ID,
                TATA_BANK_ACCOUNT_ID,
                BigDecimal.TEN,
                Currency.INR
        ));
    }

    /**
     * Test transaction creation with zero amount
     *
     * @throws ObjectModificationException
     */
    @Test(expectedExceptions = ObjectModificationException.class)
    public void testCreateTransactionWithZeroAmount() throws ObjectModificationException {
        transactionService.createTransaction(new Transaction(
                TATA_BANK_ACCOUNT_ID,
                SIEMENS_BANK_ACCOUNT_ID,
                BigDecimal.ZERO,
                Currency.INR
        ));
    }

    /**
     * Testing of Transaction creation and execution. Once transaction has been created
     * the scheduled job will execute it.
     *
     * @throws ObjectModificationException
     */
    @Test
    public void testCreateTransaction() throws ObjectModificationException {
        Long TRANSACTION_ID = 3487L;

        TransactionDao transactionDao = mock(TransactionDao.class);

        Transaction transaction = new Transaction(
                TATA_BANK_ACCOUNT_ID,
                SIEMENS_BANK_ACCOUNT_ID,
                BigDecimal.TEN,
                Currency.INR
        );
        transaction.setId(TRANSACTION_ID);

        when(transactionDao.createTransaction(any())).thenReturn(transaction);

        when(transactionDao.getAllTransactionIdsByStatus(any())).thenReturn(
                Collections.singletonList(transaction.getId())
        );

        doAnswer(invocation -> {
            transaction.setStatus(TransactionStatus.SUCCEED);
            return null;
        }).when(transactionDao).executeTransaction(anyLong());

        TransactionsService transactionsServiceImpl = ServiceFactory.createServices().getTransactionsService();
        ((TransactionsServiceImpl) transactionsServiceImpl).setTransactionDao(transactionDao);
        Transaction createdTransaction = transactionsServiceImpl.createTransaction(transaction);

        assertEquals(createdTransaction, transaction);
        assertEquals(createdTransaction.getStatus(), TransactionStatus.CREATED);

        transactionsServiceImpl.executeTransactions();

        assertEquals(transaction.getStatus(), TransactionStatus.SUCCEED);
    }
}
