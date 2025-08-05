package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.properties;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;


@Setter
@Getter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.wallet.transaction")
public class TransactionProperties {
    @NotNull
    @PositiveOrZero
    private Long maxAllowedAmount;


    public FinancialMoney getMaxAllowedAmountPerTransaction() {
        return new FinancialMoney(new BigDecimal(getMaxAllowedAmount()), CurrencyType.UNKNOWN);
    }
}
