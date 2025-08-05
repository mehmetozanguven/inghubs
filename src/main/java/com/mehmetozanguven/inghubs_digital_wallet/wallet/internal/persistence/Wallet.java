package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.entity.ApiBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@With
@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets")
public class Wallet extends ApiBaseEntity {
    @NotBlank
    @Size(max = 60)
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @NotBlank()
    @Size(max = 100)
    @Column(name = "wallet_name", nullable = false)
    private String walletName;

    @NotNull
    @Column(name = "currency_type", nullable = false)
    private CurrencyType currencyType;

    @NotNull
    @Column(name = "active_for_shopping", nullable = false)
    private Boolean activeForShopping;

    @NotNull()
    @Column(name = "active_for_withdraw", nullable = false)
    private Boolean activeForWithdraw;

    @NotNull()
    @PositiveOrZero(message = "Balance must be positive or zero")
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull()
    @PositiveOrZero(message = "Usable balance must be positive or zero")
    @Column(name = "usable_balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal usableBalance = BigDecimal.ZERO;

    public void setBalance(FinancialMoney financialMoney) {
        this.balance = financialMoney.amount();
    }

    public FinancialMoney getBalance() {
        return new FinancialMoney(balance, currencyType);
    }

    public void setUsableBalance(FinancialMoney financialMoney) {
        this.usableBalance = financialMoney.amount();
    }

    public FinancialMoney getUsableBalance() {
        return new FinancialMoney(usableBalance, currencyType);
    }
}
