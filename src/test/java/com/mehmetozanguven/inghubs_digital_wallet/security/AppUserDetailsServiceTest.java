package com.mehmetozanguven.inghubs_digital_wallet.security;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppSecureUser;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerExternalService;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerModel;
import com.mehmetozanguven.inghubs_digital_wallet.customer.CustomerRoleModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {
    @Mock
    CustomerExternalService customerExternalService;

    AppUserDetailsService appUserDetailsService;

    @BeforeEach
    void setup() {
        appUserDetailsService = new AppUserDetailsService(customerExternalService);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenCustomerNotFound() {
        when(customerExternalService.findCustomerByEmailAddress("test")).thenReturn(Optional.empty());
        Assertions.assertThrows(UsernameNotFoundException.class, () -> appUserDetailsService.loadUserByUsername("test"));
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenAccountDisabled() {
        when(customerExternalService.findCustomerByEmailAddress("test")).thenReturn(Optional.of(
                CustomerModel.builder()
                        .enabled(false)
                        .email("test")
                        .name("test")
                        .build()
        ));
        Assertions.assertThrows(DisabledException.class, () -> appUserDetailsService.loadUserByUsername("test"));
    }

    @Test
    void loadUserByUsername_ShouldReturnUser_WhenCustomerFound() {
        when(customerExternalService.findCustomerByEmailAddress("test")).thenReturn(Optional.of(
                CustomerModel.builder()
                        .enabled(true)
                        .id("test-id")
                        .email("test")
                        .name("test")
                        .surname("test")
                        .tckn("1234")
                        .customerRoles(Set.of(
                                CustomerRoleModel.builder()
                                        .appRole(AppRole.EMPLOYEE)
                                        .build()
                        ))
                        .build()
        ));
        AppSecureUser appSecureUser = (AppSecureUser) appUserDetailsService.loadUserByUsername("test");
        Assertions.assertAll(
                () -> Assertions.assertEquals("test-id", appSecureUser.getUserId()),
                () -> Assertions.assertEquals("test", appSecureUser.getUsername()),
                () -> Assertions.assertTrue(appSecureUser.isEnabled()),
                () -> Assertions.assertEquals(1, appSecureUser.getUserRoles().size()),
                () -> Assertions.assertEquals(AppRole.EMPLOYEE.getWithRoleSuffix(), appSecureUser.getUserRoles().getFirst())
        );
    }
}