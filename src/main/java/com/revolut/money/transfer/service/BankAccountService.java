package com.revolut.money.transfer.service;

import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.BankAccount;

import java.util.Collection;

public interface BankAccountService {

    Collection<BankAccount> getAllBankAccounts();

    BankAccount getBankAccountById(Long id);

    void updateBankAccount(BankAccount bankAccount) throws ObjectModificationException;

    BankAccount createBankAccount(BankAccount bankAccount) throws ObjectModificationException;

}
