package com.mehmetozanguven.inghubs_digital_wallet.security;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppSecureUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticatedUser {

    public static AppSecureUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return  (AppSecureUser) authentication.getPrincipal();
    }
}
