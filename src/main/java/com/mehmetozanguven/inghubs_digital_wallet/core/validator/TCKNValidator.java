package com.mehmetozanguven.inghubs_digital_wallet.core.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TCKNValidator implements ConstraintValidator<TCKNConstraint, String> {
    @Override
    public boolean isValid(String tcknValue, ConstraintValidatorContext constraintValidatorContext) {
        if(tcknValue.length() !=11 || tcknValue.charAt(0) =='0'){
            return false;
        }

        try {
            Long.parseLong(tcknValue);
        } catch (NumberFormatException e) {
            return false;
        }

        int oddSum=0,evenSum=0,controlDigit=0;
        for(int index = 0; index <= 8; index++){
            if(index % 2==0){
                oddSum+=Character.getNumericValue(tcknValue.charAt(index));
            } else{
                evenSum+=Character.getNumericValue(tcknValue.charAt(index));
            }
        }
        controlDigit = (oddSum*7-evenSum)%10;
        if(Character.getNumericValue(tcknValue.charAt(9))!=controlDigit) {
            return false;
        }
        if(Character.getNumericValue(tcknValue.charAt(10))!=(controlDigit+evenSum+oddSum)%10) {
            return false;
        }
        return true;

    }
}
