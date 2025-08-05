package com.mehmetozanguven.inghubs_digital_wallet.core.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TCKNValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TCKNConstraint {
    String message() default "Invalid TCKN number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
