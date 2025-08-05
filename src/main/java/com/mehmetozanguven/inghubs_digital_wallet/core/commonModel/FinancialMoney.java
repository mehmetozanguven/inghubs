package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel;

import lombok.Builder;
import lombok.With;

import java.math.BigDecimal;
import java.math.RoundingMode;

@With
@Builder
public record FinancialMoney(BigDecimal amount, CurrencyType currency) {
    public FinancialMoney(BigDecimal amount, CurrencyType currency) {
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public static FinancialMoney addMoney(FinancialMoney money, FinancialMoney anotherMoney) {
        if (money.currency().equals(anotherMoney.currency())) {
            BigDecimal amount = money.amount().add(anotherMoney.amount());
            return new FinancialMoney(amount, money.currency());
        }
        return money;
    }

    public static FinancialMoney subtractMoney(FinancialMoney money, FinancialMoney anotherMoney) {
        if (money.currency().equals(anotherMoney.currency())) {
            BigDecimal amount = money.amount().subtract(anotherMoney.amount());
            return new FinancialMoney(amount, money.currency());
        }
        return money;
    }

    public static boolean isMoreThanGivenAmount(FinancialMoney money, FinancialMoney givenMoney) {
        return money.amount().compareTo(givenMoney.amount()) > 0;
    }

    public static boolean isLessThanGivenAmount(FinancialMoney money, FinancialMoney givenMoney) {
        return money.amount().compareTo(givenMoney.amount()) < 0;
    }

    public static boolean isEqualAmount(FinancialMoney money, FinancialMoney givenMoney) {
        return money.amount().compareTo(givenMoney.amount()) == 0;
    }
}
