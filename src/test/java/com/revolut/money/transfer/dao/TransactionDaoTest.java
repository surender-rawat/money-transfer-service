package com.revolut.money.transfer.dao;

import com.revolut.money.transfer.core.ServiceFactory;
import com.revolut.money.transfer.db.DaoManager;
import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.BankAccount;
import com.revolut.money.transfer.model.Currency;
import com.revolut.money.transfer.model.Transaction;
import com.revolut.money.transfer.model.TransactionStatus;
import com.revolut.money.transfer.service.MoneyExchangeService;
import com.revolut.money.transfer.utils.Constants;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.revolut.money.transfer.utils.Constants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class TransactionDaoTest {
    private TransactionDao transactionDao;
    private Collection<Transaction> testList;
    private MoneyExchangeService moneyExchangeService;

    private static final Long TRANSACTION_1_ID = 1L;
    private static final Long TRANSACTION_2_ID = 2L;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeClass
    public void initTestData() {
        DaoManager daoManager = mock(DaoManager.class);
        transactionDao = new TransactionDao(daoManager);
        moneyExchangeService = ServiceFactory.createServices().getMoneyExchangeService();

        transaction1 = new Transaction(
                TATA_BANK_ACCOUNT_ID,
                SIEMENS_BANK_ACCOUNT_ID,
                BigDecimal.ONE,
                Currency.EUR);
        transaction1.setId(TRANSACTION_1_ID);

        transaction2 = new Transaction(
                SIEMENS_BANK_ACCOUNT_ID,
                NAGARRO_BANK_ACCOUNT_ID,
                BigDecimal.TEN,
                Currency.EUR);
        transaction2.setId(TRANSACTION_2_ID);

        testList = Arrays.asList(transaction1, transaction2);

        when(daoManager.executeQuery(eq(TransactionDao.GET_ALL_TRANSACTIONS_SQL), any())).thenReturn(
                new DaoManager.QueryResult<>(testList)
        );

        when(daoManager.executeQuery(eq(TransactionDao.GET_TRANSACTIONS_BY_STATUS_SQL), any())).thenReturn(
                new DaoManager.QueryResult<>(testList.stream().map(Transaction::getId).collect(Collectors.toList()))
        );

        when(daoManager.executeQueryInConnection(any(), eq(TransactionDao.GET_TRANSACTIONS_FOR_UPDATE_BY_ID_SQL), any()))
                .thenReturn(new DaoManager.QueryResult<>(testList));
    }

    /**
     * Tests that all transactions from DB will be returned
     */
    @Test
    public void testGetAllTransactions() {
        Collection<Transaction> resultList = transactionDao.getAllTransactions();

        assertNotNull(resultList);
        assertEquals(testList, resultList);
    }

    /**
     * Tests that all transaction's id with particular status will be returned
     */
    @Test
    public void testGetAllTransactionIdsByStatus() {
        Collection<Long> resultTransactionIds = transactionDao.getAllTransactionIdsByStatus(TransactionStatus.CREATED);

        assertNotNull(resultTransactionIds);
        assertEquals(resultTransactionIds.size(), 2);
        assertTrue(resultTransactionIds.contains(Constants.TATA_BANK_ACCOUNT_ID));
        assertTrue(resultTransactionIds.contains(SIEMENS_BANK_ACCOUNT_ID));
    }

    @Test
    public void testTransactionCreation() throws ObjectModificationException {
        TransactionDao transactionDao = TransactionDao.getInstance(moneyExchangeService);
        BankAccountDao bankAccountDao = BankAccountDao.getInstance();

        BankAccount sergey = bankAccountDao.getBankAccountById(Constants.TATA_BANK_ACCOUNT_ID);
        BankAccount nikolay = bankAccountDao.getBankAccountById(SIEMENS_BANK_ACCOUNT_ID);

        BigDecimal sergeyInitialBalance = sergey.getBalance();
        BigDecimal sergeyInitialBlocked = sergey.getBlockedAmount();
        BigDecimal nikolayInitialBalance = nikolay.getBalance();
        BigDecimal nikolayInitialBlocked = nikolay.getBlockedAmount();

        Transaction resultTransaction = transactionDao.createTransaction(transaction1);

        assertEquals(resultTransaction.getStatus(), TransactionStatus.CREATED);

        sergey = bankAccountDao.getBankAccountById(Constants.TATA_BANK_ACCOUNT_ID);
        nikolay = bankAccountDao.getBankAccountById(SIEMENS_BANK_ACCOUNT_ID);

        assertThat(sergeyInitialBalance, Matchers.comparesEqualTo(sergey.getBalance()));
        assertThat(sergeyInitialBlocked.add(
                moneyExchangeService.exchange(transaction1.getAmount(), transaction1.getCurrency(), sergey.getCurrency())),
                Matchers.comparesEqualTo(sergey.getBlockedAmount()));
        assertThat(nikolayInitialBalance, Matchers.comparesEqualTo(nikolay.getBalance()));
        assertThat(nikolayInitialBlocked, Matchers.comparesEqualTo(nikolay.getBlockedAmount()));
    }

    @Test
    public void testTransactionExecution() throws ObjectModificationException {
        TransactionDao transactionDao = TransactionDao.getInstance(moneyExchangeService);
        BankAccountDao bankAccountDao = BankAccountDao.getInstance();

        BankAccount nikolay = bankAccountDao.getBankAccountById(SIEMENS_BANK_ACCOUNT_ID);
        BankAccount vlad = bankAccountDao.getBankAccountById(Constants.NAGARRO_BANK_ACCOUNT_ID);

        BigDecimal nikolayInitialBalance = nikolay.getBalance();
        BigDecimal nikolayInitialBlocked = nikolay.getBlockedAmount();
        BigDecimal vladInitialBalance = vlad.getBalance();
        BigDecimal vladInitialBlocked = vlad.getBlockedAmount();

        Transaction resultTransaction = transactionDao.createTransaction(transaction2);
        transactionDao.executeTransaction(resultTransaction.getId());

        resultTransaction = transactionDao.getTransactionById(resultTransaction.getId());
        nikolay = bankAccountDao.getBankAccountById(transaction2.getFromBankAccountId());
        vlad = bankAccountDao.getBankAccountById(transaction2.getToBankAccountId());
        BigDecimal needToWithdraw = moneyExchangeService.exchange(
                transaction2.getAmount(),
                transaction2.getCurrency(),
                nikolay.getCurrency()
        );
        BigDecimal needToTransfer = moneyExchangeService.exchange(
                transaction2.getAmount(),
                transaction2.getCurrency(),
                vlad.getCurrency()
        );

        assertEquals(resultTransaction.getStatus(), TransactionStatus.SUCCEED);
        assertThat(nikolayInitialBalance.subtract(needToWithdraw), Matchers.comparesEqualTo(nikolay.getBalance()));

        assertThat(nikolayInitialBlocked, Matchers.comparesEqualTo(nikolay.getBlockedAmount()));

        assertThat(vladInitialBalance.add(needToTransfer), Matchers.comparesEqualTo(vlad.getBalance()));

        assertThat(vladInitialBlocked, Matchers.comparesEqualTo(vlad.getBlockedAmount()));
    }

    @Test(expectedExceptions = ObjectModificationException.class)
    public void testWrongTransactionCreation() throws ObjectModificationException {
        TransactionDao transactionDao = TransactionDao.getInstance(moneyExchangeService);
        BankAccountDao bankAccountDao = BankAccountDao.getInstance();

        Transaction transaction = new Transaction(
                SIEMENS_BANK_ACCOUNT_ID,
                Constants.NAGARRO_BANK_ACCOUNT_ID,
                BigDecimal.valueOf(10000), //much more than Nikolay has
                Currency.EUR
        );

        transactionDao.createTransaction(transaction);
    }
}
