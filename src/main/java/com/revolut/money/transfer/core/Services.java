package com.revolut.money.transfer.core;


import com.revolut.money.transfer.service.BankAccountServiceImpl;
import com.revolut.money.transfer.service.MoneyExchangeService;
import com.revolut.money.transfer.service.TransactionsService;

/**
 * Provides access to all of the services so that circular dependencies between them can be resolved.
 */
public interface Services {


    BankAccountServiceImpl getAccountService();

    TransactionsService getTransactionsService();

    MoneyExchangeService getMoneyExchangeService();

}
