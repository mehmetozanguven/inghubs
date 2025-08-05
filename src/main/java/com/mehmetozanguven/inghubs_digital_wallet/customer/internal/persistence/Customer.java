package com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence;

import com.mehmetozanguven.inghubs_digital_wallet.core.entity.ApiBaseEntity;
import com.mehmetozanguven.inghubs_digital_wallet.core.validator.TCKNConstraint;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@With
@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")
public class Customer extends ApiBaseEntity {
    @Email
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank
    @Size(max = 30)
    @Column(name = "name")
    private String name;

    @NotBlank
    @Size(max = 30)
    @Column(name = "surname")
    private String surname;

    @NotBlank
    @Column(name = "password")
    private String password;

    @NotBlank
    @Size(max = 11)
    @Column(name = "tckn")
    @TCKNConstraint
    private String tckn;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER, cascade ={
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(name = "customer_roles_join",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "customer_role_id")
    )
    private Set<CustomerRole> customerRoles = new HashSet<>();

    public void addUserRole(CustomerRole customerRole) {
        customerRoles.add(customerRole);
    }

    public void removeUserRole(CustomerRole customerRole) {
        customerRoles.remove(customerRole);
    }

}
