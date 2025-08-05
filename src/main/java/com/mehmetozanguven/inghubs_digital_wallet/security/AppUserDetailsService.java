package com.mehmetozanguven.inghubs_digital_wallet.security;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppSecureUser;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerModel;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerExternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;


@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    public static final String DISABLED_ACCOUNT_EXCEPTION = "Account is disabled";
    private final CustomerExternalService customerExternalService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CustomerModel> inDb = customerExternalService.findCustomerByEmailAddress(username);

        if (inDb.isEmpty()) {
            throw new UsernameNotFoundException("Invalid username");
        }

        CustomerModel userInfo = inDb.get();

        AppSecureUser appSecureUser = AppSecureUser.fromUserModel(
                userInfo.getId(), userInfo.getEmail(), userInfo.getPassword(),
                userInfo.getRolesAsStrings(),
                userInfo.isEnabled()
        );
        if (!appSecureUser.isEnabled()) {
            throw new DisabledException(DISABLED_ACCOUNT_EXCEPTION);
        }
        return appSecureUser;
    }


}
