package com.revolut.money.transfer.core;


import com.revolut.money.transfer.service.BankAccountServiceImpl;
import com.revolut.money.transfer.service.RevolutMoneyExchangeService;
import com.revolut.money.transfer.service.MoneyExchangeService;
import com.revolut.money.transfer.service.TransactionsService;
import com.revolut.money.transfer.service.TransactionsServiceImpl;

/**
 * Factory for building a new services
 */
public class ServiceFactory implements Services {
    private BankAccountServiceImpl accountService;
    private TransactionsService transactionsService;
    private MoneyExchangeService moneyExchangeService;
    private static ServiceFactory factory = null;

    private ServiceFactory() {

    }

    public static Services createServices() {
        if (null == factory)
            factory = new ServiceFactory();
        return factory;
    }


    public BankAccountServiceImpl getAccountService() {
        if (null == accountService) {
            accountService = BankAccountServiceImpl.getInstance(this);
        }
        return accountService;
    }

    public TransactionsService getTransactionsService() {
        if (null == transactionsService)
            transactionsService = TransactionsServiceImpl.getInstance(this);

        return transactionsService;
    }

    public MoneyExchangeService getMoneyExchangeService() {
        if (null == moneyExchangeService)
            moneyExchangeService = RevolutMoneyExchangeService.getInstance(this);

        return moneyExchangeService;
    }
}
