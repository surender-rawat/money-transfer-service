package com.revolut.money.transfer.dao;

import com.revolut.money.transfer.db.DaoManager;
import com.revolut.money.transfer.exceptions.ExceptionType;
import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.BankAccount;
import com.revolut.money.transfer.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base DAO
 */
public class BaseDao {


    protected DaoManager daoManager = DaoManager.getInstance();

    protected Connection getConnection() throws SQLException {
        return daoManager.getConnection();
    }

    /**
     * Verifies the validity of the Transaction object to be saved into the database.
     *
     * @param transaction Transaction object to be validated
     * @throws ObjectModificationException in case of any invalid parameter
     */
    protected void verify(Transaction transaction) throws ObjectModificationException {
        if (transaction.getAmount() == null || transaction.getFromBankAccountId() == null ||
                transaction.getToBankAccountId() == null || transaction.getCurrency() == null
                || transaction.getStatus() == null || transaction.getCreationDate() == null
                || transaction.getUpdateDate() == null) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED, "Fields could not be NULL");
        }
    }

    /**
     * Verifies the validity of the Bank Account object to be saved into the database.
     *
     * @param bankAccount Bank Account object to be validated
     * @throws ObjectModificationException in case of any invalid parameter
     */
    protected void verify(BankAccount bankAccount) throws ObjectModificationException {
        if (bankAccount.getId() == null) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED,
                    "ID value is invalid");
        }

        if (bankAccount.getOwnerName() == null || bankAccount.getBalance() == null ||
                bankAccount.getBlockedAmount() == null || bankAccount.getCurrency() == null) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED, "Fields could not be NULL");
        }
    }


}
