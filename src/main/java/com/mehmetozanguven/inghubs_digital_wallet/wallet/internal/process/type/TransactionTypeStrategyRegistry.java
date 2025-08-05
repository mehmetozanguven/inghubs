package com.mehmetozanguven.inghubs_digital_wallet.wallet.internal.process.type;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TransactionTypeStrategyRegistry {

    private final Map<TransactionType, TransactionTypeStrategy> strategyMap = new EnumMap<>(TransactionType.class);

    @Autowired
    public TransactionTypeStrategyRegistry(List<TransactionTypeStrategy> strategies) {
        for (TransactionTypeStrategy strategy : strategies) {
            TransactionTypeHandler annotation = strategy.getClass().getAnnotation(TransactionTypeHandler.class);
            if (annotation != null) {
                strategyMap.put(annotation.value(), strategy);
            }
        }
    }

    public TransactionTypeStrategy getHandler(TransactionType type) {
        return Optional.ofNullable(strategyMap.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for transaction type: " + type));
    }
}
