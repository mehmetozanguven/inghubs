package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.opposite_party_type;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OppositePartyTypeConverter implements AttributeConverter<OppositePartyType, String> {
    @Override
    public String convertToDatabaseColumn(OppositePartyType attribute) {
        return attribute.dbValue;
    }

    @Override
    public OppositePartyType convertToEntityAttribute(String dbData) {
        return OppositePartyType.findByDBValue(dbData);
    }
}
