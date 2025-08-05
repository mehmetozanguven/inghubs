package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.status;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TransactionStatusStrategyRegistry {

    private final Map<TransactionStatus, TransactionStatusStrategy> strategyMap = new EnumMap<>(TransactionStatus.class);
    private final TransactionStatusStrategy emptyTransactionStrategy;

    @Autowired
    public TransactionStatusStrategyRegistry(List<TransactionStatusStrategy> strategies, UnknownTransactionStatusStrategy unknownTransactionStatusStrategy) {
        this.emptyTransactionStrategy = unknownTransactionStatusStrategy;
        for (TransactionStatusStrategy strategy : strategies) {
            TransactionStatusHandler annotation = strategy.getClass().getAnnotation(TransactionStatusHandler.class);
            if (annotation != null) {
                strategyMap.put(annotation.value(), strategy);
            }
        }
    }

    public TransactionStatusStrategy getHandler(TransactionStatus type) {
        return Optional.ofNullable(strategyMap.get(type))
                .orElse(emptyTransactionStrategy);
    }
}
