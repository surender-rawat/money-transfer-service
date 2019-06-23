package com.revolut.money.transfer.utils;

public class Constants {
    public static final String BLANK = "";
    public static final Long TATA_BANK_ACCOUNT_ID = 1L;
    public static final Long SIEMENS_BANK_ACCOUNT_ID = 2L;
    public static final Long NAGARRO_BANK_ACCOUNT_ID = 3L;

    // Bank Account Table column
    public static final String BANK_ACCOUNT_TABLE_NAME = "bank_account";
    public static final String BANK_ACCOUNT_ID_ROW = "id";
    public static final String BANK_ACCOUNT_OWNER_NAME_ROW = "owner_name";
    public static final String BANK_ACCOUNT_BALANCE_ROW = "balance";
    public static final String BANK_ACCOUNT_BLOCKED_AMOUNT_ROW = "blocked_amount";
    public static final String BANK_ACCOUNT_CURRENCY_ID_ROW = "currency_id";

    //Transaction Table Column
    public static final String TRANSACTION_TABLE_NAME = "transaction";
    public static final String TRANSACTION_ID_ROW = "id";
    public static final String TRANSACTION_FROM_ACCOUNT_ROW = "from_account_id";
    public static final String TRANSACTION_TO_ACCOUNT_ROW = "to_account_id";
    public static final String TRANSACTION_AMOUNT_ROW = "amount";
    public static final String TRANSACTION_CURRENCY_ROW = "currency_id";
    public static final String TRANSACTION_CREATION_DATE_ROW = "creation_date";
    public static final String TRANSACTION_UPDATE_DATE_ROW = "update_date";
    public static final String TRANSACTION_STATUS_ROW = "status_id";
    public static final String FAIL_MESSAGE_ROW = "failMessage";

}
