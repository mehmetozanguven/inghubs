package com.mehmetozanguven.inghubs_digital_wallet.customer;

import lombok.Builder;

@Builder
public record CreateCustomerRequest(String email, String password, String name, String surname, String tckn) {
}
