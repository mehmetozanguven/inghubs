package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.roleConverter;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AppRoleJPAConverter implements AttributeConverter<AppRole, String> {
    @Override
    public String convertToDatabaseColumn(AppRole attribute) {
        return attribute.plainRole;
    }

    @Override
    public AppRole convertToEntityAttribute(String dbData) {
        return AppRole.findByPlainRole(dbData);
    }
}
