package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.transaction;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {
    @Override
    public String convertToDatabaseColumn(TransactionType attribute) {
        return attribute.dbValue;
    }

    @Override
    public TransactionType convertToEntityAttribute(String dbData) {
        return TransactionType.findByDBValue(dbData);
    }
}
