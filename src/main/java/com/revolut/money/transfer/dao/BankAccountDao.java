package com.revolut.money.transfer.dao;

import com.revolut.money.transfer.db.DaoManager;
import com.revolut.money.transfer.exceptions.ExceptionType;
import com.revolut.money.transfer.exceptions.ObjectModificationException;
import com.revolut.money.transfer.model.BankAccount;
import com.revolut.money.transfer.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static com.revolut.money.transfer.utils.Constants.*;


/**
 * Encapsulates all logic for Bank Account entity which is related to the database. Implements the singleton pattern.
 */
public class BankAccountDao extends BaseDao{



    private static final Logger log = LoggerFactory.getLogger(BankAccountDao.class);

    private static final BankAccountDao bas = new BankAccountDao();


    private BankAccountDao() {
    }

    public static BankAccountDao getInstance() {
        return bas;
    }

    /**
     * @return All Bank Accounts which is exists in the database at the moment
     */
    public Collection<BankAccount> getAllBankAccounts() {
        return daoManager.executeQuery("select * from " + BANK_ACCOUNT_TABLE_NAME, getBankAccounts -> {
            Collection<BankAccount> bankAccounts = new ArrayList<>();

            try (ResultSet bankAccountsRS = getBankAccounts.executeQuery()) {
                if (bankAccountsRS != null) {
                    while (bankAccountsRS.next()) {
                        bankAccounts.add(extractBankAccountFromResultSet(bankAccountsRS));
                    }
                }
            }

            return bankAccounts;
        }).getResult();
    }

    /**
     * Returns Bank Account object by id specified
     *
     * @param id Bank Account object id
     * @return Bank Account object with id specified
     */
    public BankAccount getBankAccountById(Long id) {
        String GET_BANK_ACCOUNT_BY_ID_SQL =
                "select * from " + BANK_ACCOUNT_TABLE_NAME + " ba " +
                        "where ba." + BANK_ACCOUNT_ID_ROW + " = ?";

        return daoManager.executeQuery(GET_BANK_ACCOUNT_BY_ID_SQL, getBankAccount -> {
            getBankAccount.setLong(1, id);
            try (ResultSet bankAccountRS = getBankAccount.executeQuery()) {
                if (bankAccountRS != null && bankAccountRS.first()) {
                    return extractBankAccountFromResultSet(bankAccountRS);
                }
            }

            return null;
        }).getResult();
    }

    /**
     * Special form of {@link #getBankAccountById(Long)} method which is not closing the connection once result
     * will be obtained. We are using it only inside the related <code>TransactionDao</code>
     *
     * @param id  Bank Account object id
     * @param con the <code>Connection</code> to be used for this query
     */
    BankAccount getForUpdateBankAccountById(Connection con, Long id) {
        String GET_BANK_ACCOUNT_BY_ID_SQL =
                "select * from " + BANK_ACCOUNT_TABLE_NAME + " ba " +
                        "where ba." + BANK_ACCOUNT_ID_ROW + " = ? " +
                        "for update";

        return daoManager.executeQueryInConnection(con, GET_BANK_ACCOUNT_BY_ID_SQL, getBankAccount -> {
            getBankAccount.setLong(1, id);
            try (ResultSet bankAccountRS = getBankAccount.executeQuery()) {
                if (bankAccountRS != null && bankAccountRS.first()) {
                    return extractBankAccountFromResultSet(bankAccountRS);
                }
            }

            return null;
        }).getResult();
    }

    /**
     * Updates the Bank Account with changed parameters using the id provided by the object passed. Only ownerName
     * parameter will be updated.
     *
     * @param bankAccount - the object to be updated
     * @throws ObjectModificationException if Bank Account with the provided id will not be exists in the database at
     *                                     the moment or object provided is malformed
     */
    public void updateBankAccountSafe(BankAccount bankAccount) throws ObjectModificationException {
        String UPDATE_BANK_ACCOUNT_SQL =
                "update " + BANK_ACCOUNT_TABLE_NAME +
                        " set " +
                        BANK_ACCOUNT_OWNER_NAME_ROW + " = ? " +
                        "where " + BANK_ACCOUNT_ID_ROW + " = ?";

        if (bankAccount.getId() == null || bankAccount.getOwnerName() == null) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED, "Id and OwnerName fields could not be NULL");
        }

        DaoManager.QueryExecutor<Integer> queryExecutor = updateBankAccount -> {
            updateBankAccount.setString(1, bankAccount.getOwnerName());
            updateBankAccount.setLong(2, bankAccount.getId());

            return updateBankAccount.executeUpdate();
        };

        int result = daoManager.executeQuery(UPDATE_BANK_ACCOUNT_SQL, queryExecutor).getResult();

        if (result == 0) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_NOT_FOUND);
        }
    }

    /**
     * Updates the Bank Account with changed parameters using the id provided by the object passed.
     * We are using it only inside the related <code>TransactionDao</code>
     *
     * @param bankAccount Bank Account object which will be updated
     * @param con         the <code>Connection</code> to be used for this query
     * @throws ObjectModificationException if Bank Account with the provided id will not be exists in the database at the
     *                                     moment or object provided is malformed
     */
    void updateBankAccount(BankAccount bankAccount, Connection con) throws ObjectModificationException {
        String UPDATE_BANK_ACCOUNT_SQL =
                "update " + BANK_ACCOUNT_TABLE_NAME +
                        " set " +
                        BANK_ACCOUNT_OWNER_NAME_ROW + " = ?, " +
                        BANK_ACCOUNT_BALANCE_ROW + " = ?, " +
                        BANK_ACCOUNT_BLOCKED_AMOUNT_ROW + " = ?, " +
                        BANK_ACCOUNT_CURRENCY_ID_ROW + " = ? " +
                        "where " + BANK_ACCOUNT_ID_ROW + " = ?";

        verify(bankAccount);

        DaoManager.QueryExecutor<Integer> queryExecutor = updateBankAccount -> {
            fillInPreparedStatement(updateBankAccount, bankAccount);
            updateBankAccount.setLong(5, bankAccount.getId());

            return updateBankAccount.executeUpdate();
        };

        int result;
        if (con == null) {
            result = daoManager.executeQuery(UPDATE_BANK_ACCOUNT_SQL, queryExecutor).getResult();
        } else {
            result = daoManager.executeQueryInConnection(con, UPDATE_BANK_ACCOUNT_SQL, queryExecutor).getResult();
        }

        if (result == 0) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_NOT_FOUND);
        }
    }

    /**
     * Creates the Bank Account object provided in the database. Id of this objects will not be used. It will be
     * generated and returned in the result of the method.
     *
     * @param bankAccount Bank Account object which should be created
     * @return created Bank Account object with ID specified'
     * @throws ObjectModificationException if Bank Account with the provided id will not be exists in the database at the
     *                                     moment or object provided is malformed
     */
    public BankAccount createBankAccount(BankAccount bankAccount) throws ObjectModificationException {
        String INSERT_BANK_ACCOUNT_SQL =
                "insert into " + BANK_ACCOUNT_TABLE_NAME +
                        " (" +
                        BANK_ACCOUNT_OWNER_NAME_ROW + ", " +
                        BANK_ACCOUNT_BALANCE_ROW + ", " +
                        BANK_ACCOUNT_BLOCKED_AMOUNT_ROW + ", " +
                        BANK_ACCOUNT_CURRENCY_ID_ROW +
                        ") values (?, ?, ?, ?)";

        verify(bankAccount);

        bankAccount = daoManager.executeQuery(INSERT_BANK_ACCOUNT_SQL,
                new DaoManager.CreationQueryExecutor<>(bankAccount, BankAccountDao::fillInPreparedStatement)).getResult();

        if (bankAccount == null) {
            throw new ObjectModificationException(ExceptionType.COULD_NOT_OBTAIN_ID);
        }

        return bankAccount;
    }

    /**
     * The opposite method to {@link #fillInPreparedStatement(PreparedStatement, BankAccount)} which is
     * extracts Bank Account parameters from the result set
     *
     * @param bankAccountsRS result set with parameters of the Bank Account
     * @return extracted Bank Account object
     * @throws SQLException if some parameters in result set will not be found or will have non compatible
     *                      data type
     */
    private BankAccount extractBankAccountFromResultSet(ResultSet bankAccountsRS) throws SQLException {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(bankAccountsRS.getLong(BANK_ACCOUNT_ID_ROW));
        bankAccount.setOwnerName(bankAccountsRS.getString(BANK_ACCOUNT_OWNER_NAME_ROW));
        bankAccount.setBalance(bankAccountsRS.getBigDecimal(BANK_ACCOUNT_BALANCE_ROW));
        bankAccount.setBlockedAmount(bankAccountsRS.getBigDecimal(BANK_ACCOUNT_BLOCKED_AMOUNT_ROW));
        bankAccount.setCurrency(Currency.valueOf(bankAccountsRS.getInt(BANK_ACCOUNT_CURRENCY_ID_ROW)));

        return bankAccount;
    }


    /**
     * Fills the provided prepared statement with the Bank Account's parameters provided
     *
     * @param preparedStatement prepared statement to be filled in
     * @param bankAccount       the Bank Account object which should be used to fill in
     */
    private static void fillInPreparedStatement(PreparedStatement preparedStatement, BankAccount bankAccount) {
        try {
            preparedStatement.setString(1, bankAccount.getOwnerName());
            preparedStatement.setBigDecimal(2, bankAccount.getBalance());
            preparedStatement.setBigDecimal(3, bankAccount.getBlockedAmount());
            preparedStatement.setLong(4, bankAccount.getCurrency().getId());
        } catch (SQLException e) {
            log.error("BankAccount prepared statement could not be initialized by values", e);
        }
    }
}
