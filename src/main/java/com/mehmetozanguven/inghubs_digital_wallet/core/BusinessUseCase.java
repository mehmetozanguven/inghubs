package com.mehmetozanguven.inghubs_digital_wallet.core;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface BusinessUseCase {
}
