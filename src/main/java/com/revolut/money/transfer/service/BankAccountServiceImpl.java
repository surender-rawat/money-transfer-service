package com.revolut.money.transfer.service;

import com.revolut.money.transfer.core.AbstractService;
import com.revolut.money.transfer.core.Services;
import com.revolut.money.transfer.dao.BankAccountDao;
import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.BankAccount;

import java.util.Collection;

/**
 * Right now the proxy service under the {@link BankAccountDao}. Should be used to abstract the presentation layer
 * from the persistence layer
 */
public class BankAccountServiceImpl extends AbstractService implements BankAccountService {
    private static BankAccountServiceImpl bas = null;

    private BankAccountServiceImpl(Services services) {
        super(services);
    }

    /**
     * @param services
     * @return
     */
    public static BankAccountServiceImpl getInstance(Services services) {
        if (bas == null) {
            synchronized (BankAccountServiceImpl.class) {
                if (bas == null) {
                    bas = new BankAccountServiceImpl(services);
                }
            }
        }
        return bas;
    }


    public Collection<BankAccount> getAllBankAccounts() {
        return BankAccountDao.getInstance().getAllBankAccounts();
    }

    public BankAccount getBankAccountById(Long id) {
        return BankAccountDao.getInstance().getBankAccountById(id);
    }

    public void updateBankAccount(BankAccount bankAccount) throws ObjectModificationException {
        BankAccountDao.getInstance().updateBankAccountSafe(bankAccount);
    }

    public BankAccount createBankAccount(BankAccount bankAccount) throws ObjectModificationException {
        return BankAccountDao.getInstance().createBankAccount(bankAccount);
    }
}
