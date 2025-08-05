package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TransactionEvent(
        @NotBlank String transactionId
) {
}
