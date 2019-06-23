package com.revolut.money.transfer.integration;

import com.revolut.money.transfer.core.ServiceFactory;
import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.BankAccount;
import com.revolut.money.transfer.model.Currency;
import com.revolut.money.transfer.model.Transaction;
import com.revolut.money.transfer.service.BankAccountService;
import com.revolut.money.transfer.service.TransactionsService;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This Test create the transactions and processed it. It verify the amount in source and desination account as well.
 */
public class ConcurrentlyTransactionCreationAndExecutionTest {
    private TransactionsService transactionsServiceImpl = ServiceFactory.createServices().getTransactionsService();
    private BankAccountService bankAccountServiceImpl = ServiceFactory.createServices().getAccountService();

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(1000L);
    private static final BigDecimal TRANSACTION_AMOUNT = BigDecimal.ONE;
    private static final int INVOCATION_COUNT = 1000;

    private Long fromBankAccountId;
    private Long toBankAccountId;
    private AtomicInteger invocationsDone = new AtomicInteger(0);

    @BeforeClass
    public void initData() throws ObjectModificationException {
        BankAccount fromBankAccount = new BankAccount(
                "New Bank Account 1",
                INITIAL_BALANCE,
                BigDecimal.ZERO,
                Currency.EUR
        );

        BankAccount toBankAccount = new BankAccount(
                "New Bank Account 2",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Currency.USD
        );

        fromBankAccountId = bankAccountServiceImpl.createBankAccount(fromBankAccount).getId();
        toBankAccountId = bankAccountServiceImpl.createBankAccount(toBankAccount).getId();
    }

    @Test(threadPoolSize = 100, invocationCount = INVOCATION_COUNT)
    public void testConcurrentTransactionCreation() throws ObjectModificationException {
        int currentTestNumber = invocationsDone.addAndGet(1);
        Transaction transaction = new Transaction(
                fromBankAccountId,
                toBankAccountId,
                TRANSACTION_AMOUNT,
                Currency.EUR
        );

        transactionsServiceImpl.createTransaction(transaction);

        if (currentTestNumber % 5 == 0) {
            transactionsServiceImpl.executeTransactions();
        }
    }

    @AfterClass
    public void checkResults() {
        transactionsServiceImpl.executeTransactions();
        BankAccount fromBankAccount = bankAccountServiceImpl.getBankAccountById(fromBankAccountId);
        assertThat(fromBankAccount.getBalance(),
                Matchers.comparesEqualTo(
                        INITIAL_BALANCE.subtract(
                                TRANSACTION_AMOUNT.multiply(BigDecimal.valueOf(INVOCATION_COUNT)))
                )
        );
        assertThat(fromBankAccount.getBlockedAmount(), Matchers.comparesEqualTo(BigDecimal.ZERO));


        //Amout Transfered to second account successfully
        BankAccount toBankAccount = bankAccountServiceImpl.getBankAccountById(toBankAccountId);
        assertThat(toBankAccount.getBalance(), Matchers.comparesEqualTo(INITIAL_BALANCE.multiply(BigDecimal.valueOf(1.12))));

    }
}
