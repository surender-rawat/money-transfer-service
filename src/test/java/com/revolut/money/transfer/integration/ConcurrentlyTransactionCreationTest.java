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

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test is only creating concurrent transaction but not processing those transactions.
 */
public class ConcurrentlyTransactionCreationTest {
    private TransactionsService transactionsServiceImpl = ServiceFactory.createServices().getTransactionsService();
    private BankAccountService bankAccountServiceImpl = ServiceFactory.createServices().getAccountService();

    private static final BigDecimal INITIAL_BALANCE_ACC_1 = BigDecimal.valueOf(1000L);
    private static final BigDecimal INITIAL_BALANCE_ACC_3 = BigDecimal.valueOf(2000L);
    private static final BigDecimal TRANSACTION_AMOUNT_FROM_1_TO_2 = BigDecimal.ONE;
    private static final BigDecimal TRANSACTION_AMOUNT_FROM_3_TO_4 = BigDecimal.valueOf(800L);
    private static final int INVOCATION_COUNT = 1000;

    private Long fromBankAccountId_1;
    private Long toBankAccountId_2;
    private Long fromBankAccountId_3;
    private Long toBankAccountId_4;

    @BeforeClass
    public void initData() throws ObjectModificationException {
        BankAccount fromBankAccount_1 = new BankAccount(
                "New Bank Account 1",
                INITIAL_BALANCE_ACC_1,
                BigDecimal.ZERO,
                Currency.EUR
        );

        BankAccount toBankAccount_2 = new BankAccount(
                "New Bank Account 2",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Currency.USD
        );

        fromBankAccountId_1 = bankAccountServiceImpl.createBankAccount(fromBankAccount_1).getId();
        toBankAccountId_2 = bankAccountServiceImpl.createBankAccount(toBankAccount_2).getId();


        BankAccount fromBankAccount_3 = new BankAccount(
                "New Bank Account 3",
                INITIAL_BALANCE_ACC_3,
                BigDecimal.ZERO,
                Currency.EUR
        );

        BankAccount toBankAccount_4 = new BankAccount(
                "New Bank Account 4",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Currency.USD
        );

        fromBankAccountId_3 = bankAccountServiceImpl.createBankAccount(fromBankAccount_3).getId();
        toBankAccountId_4 = bankAccountServiceImpl.createBankAccount(toBankAccount_4).getId();


    }

    @Test(threadPoolSize = 100, invocationCount = INVOCATION_COUNT)
    public void testConcurrentTransactionCreationForAccount1And2() throws ObjectModificationException {
        Transaction transaction = new Transaction(
                fromBankAccountId_1,
                toBankAccountId_2,
                TRANSACTION_AMOUNT_FROM_1_TO_2,
                Currency.EUR
        );

        transactionsServiceImpl.createTransaction(transaction);
    }

    @Test(threadPoolSize = 2, invocationCount = 4)
    public void testConcurrentTransactionCreationForAccount3And4() throws ObjectModificationException {
        Transaction transaction = new Transaction(
                fromBankAccountId_3,
                toBankAccountId_4,
                TRANSACTION_AMOUNT_FROM_3_TO_4,
                Currency.EUR
        );
        try {
            transactionsServiceImpl.createTransaction(transaction);
        } catch (ObjectModificationException exp) {

        }
    }

    @AfterClass
    public void checkResults() {
        BankAccount bankAccount_1 = bankAccountServiceImpl.getBankAccountById(fromBankAccountId_1);

        assertThat(bankAccount_1.getBalance(), Matchers.comparesEqualTo(INITIAL_BALANCE_ACC_1));
        assertThat(bankAccount_1.getBlockedAmount(),
                Matchers.comparesEqualTo(
                        BigDecimal.ZERO.add(
                                TRANSACTION_AMOUNT_FROM_1_TO_2.multiply(BigDecimal.valueOf(INVOCATION_COUNT)))
                )
        );

        BankAccount bankAccount_3 = bankAccountServiceImpl.getBankAccountById(fromBankAccountId_3);
        assertThat(bankAccount_3.getBalance(), Matchers.comparesEqualTo(INITIAL_BALANCE_ACC_3));
        assertThat(bankAccount_3.getBlockedAmount(), Matchers.comparesEqualTo(BigDecimal.ZERO.add(TRANSACTION_AMOUNT_FROM_3_TO_4.multiply(BigDecimal.valueOf(2)))));


    }
}
