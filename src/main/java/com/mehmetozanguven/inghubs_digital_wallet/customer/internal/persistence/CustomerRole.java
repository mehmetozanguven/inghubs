package com.mehmetozanguven.inghubs_digital_wallet.customer.internal.persistence;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppRole;
import com.mehmetozanguven.inghubs_digital_wallet.core.entity.ApiBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@With
@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer_roles")
public class CustomerRole extends ApiBaseEntity {
    @NotNull
    @Column(name = "role")
    private AppRole appRole;
}
