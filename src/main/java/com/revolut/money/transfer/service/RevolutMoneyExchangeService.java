package com.revolut.money.transfer.service;

import com.revolut.money.transfer.core.AbstractService;
import com.revolut.money.transfer.core.Services;
import com.revolut.money.transfer.model.Currency;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * It's a fixed rate constant service for money exchange implementation.
 */
public class RevolutMoneyExchangeService extends AbstractService implements MoneyExchangeService {

    private static MoneyExchangeService mxs;
    private final static Map<Pair<Currency, Currency>, BigDecimal> conversionRates =
            Collections.unmodifiableMap(new HashMap<Pair<Currency, Currency>, BigDecimal>() {{
                put(new Pair<>(Currency.INR, Currency.EUR), BigDecimal.valueOf(0.013));
                put(new Pair<>(Currency.INR, Currency.USD), BigDecimal.valueOf(0.014));
                put(new Pair<>(Currency.INR, Currency.INR), BigDecimal.valueOf(1D));
                put(new Pair<>(Currency.USD, Currency.EUR), BigDecimal.valueOf(0.89));
                put(new Pair<>(Currency.USD, Currency.USD), BigDecimal.valueOf(1D));
                put(new Pair<>(Currency.USD, Currency.INR), BigDecimal.valueOf(69.46));
                put(new Pair<>(Currency.EUR, Currency.EUR), BigDecimal.valueOf(1D));
                put(new Pair<>(Currency.EUR, Currency.USD), BigDecimal.valueOf(1.12));
                put(new Pair<>(Currency.EUR, Currency.INR), BigDecimal.valueOf(77.81));
            }});

    protected RevolutMoneyExchangeService(Services services) {
        super(services);
    }

    public static MoneyExchangeService getInstance(Services services) {
        if (null == mxs) {
            mxs = new RevolutMoneyExchangeService(services);
        }
        return mxs;
    }

    @Override
    public BigDecimal exchange(BigDecimal amount, Currency amountCurrency, Currency targetCurrency) {
        return amount.multiply(conversionRates.get(new Pair<>(amountCurrency, targetCurrency)));
    }
}
