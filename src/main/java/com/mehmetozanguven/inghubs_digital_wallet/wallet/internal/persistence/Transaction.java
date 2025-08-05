package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.persistence;

import com.mehmetozanguven.inghubs_digital_wallet.core.DateOperation;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.CurrencyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.FinancialMoney;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.opposite_party_type.OppositePartyType;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import com.mehmetozanguven.inghubs_digital_wallet.core.entity.ApiBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TimeZoneColumn;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@With
@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet_transactions")
public class Transaction extends ApiBaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @NotNull
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @NotNull
    @Column(name = "transaction_status")
    private TransactionStatus transactionStatus;

    @NotNull
    @Column(name = "opposite_party_type", nullable = false)
    private OppositePartyType oppositePartyType;

    @NotNull()
    @Column(name = "opposite_party", nullable = false)
    private String oppositeParty;

    @NotNull()
    @PositiveOrZero(message = "Balance must be positive or zero")
    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "currency_type", nullable = false)
    private CurrencyType currencyType;

    @CreatedDate
    @Column(name = "processed_date")
    @TimeZoneStorage(TimeZoneStorageType.COLUMN)
    @TimeZoneColumn(
            name = "processed_date_offset",
            columnDefinition = "smallint unsigned"
    )
    private OffsetDateTime processedAt;

    @CreatedDate
    @Column(name = "expiration_time", nullable = false)
    @TimeZoneStorage(TimeZoneStorageType.COLUMN)
    @TimeZoneColumn(
            name = "expiration_time_offset",
            columnDefinition = "smallint unsigned"
    )
    private OffsetDateTime expirationTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "info_field", columnDefinition = "jsonb")
    @Builder.Default
    private TransactionInfo transactionInfo = TransactionInfo.builder().build();

    public FinancialMoney getTransactionAmount() {
        return new FinancialMoney(amount, currencyType);
    }

    public boolean getIsTransactionExpired() {
        return DateOperation.getOffsetNowAsUTC().isAfter(expirationTime);
    }

}
