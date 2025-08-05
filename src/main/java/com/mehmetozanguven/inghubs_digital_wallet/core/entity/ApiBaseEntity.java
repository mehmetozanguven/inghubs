package com.mehmetozanguven.inghubs_digital_wallet.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TimeZoneColumn;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditableListener.class)
public abstract class ApiBaseEntity implements Auditable, Serializable {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id")
    private String id;

    @Column(name = "created_date_in_ms")
    private Long createTimestampMilli;

    @CreatedDate
    @Column(name = "created_date")
    @TimeZoneStorage(TimeZoneStorageType.COLUMN)
    @TimeZoneColumn(
            name = "created_date_offset",
            columnDefinition = "smallint unsigned"
    )
    private OffsetDateTime createUTCTime;

    @LastModifiedDate
    @Column(name = "last_update_date")
    @TimeZoneStorage(TimeZoneStorageType.COLUMN)
    @TimeZoneColumn(
            name = "last_update_offset",
            columnDefinition = "smallint unsigned"
    )
    private OffsetDateTime lastModifiedUTCTime;

    @Column(name = "last_update_date_in_ms")
    private Long lastModifiedTimestampMilli;

    @Column(name = "entity_version_id")
    @Version
    private Long entityVersionId;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = Boolean.TRUE;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Long getEntityVersionId() {
        return entityVersionId;
    }

    public void setEntityVersionId(Long entityVersionId) {
        this.entityVersionId = entityVersionId;
    }

    @Override
    public Long getCreateTimestampMilli() {
        return createTimestampMilli;
    }

    @Override
    public void setCreateTimestampMilli(long createTimestampMilli) {
        this.createTimestampMilli = createTimestampMilli;
    }

    @Override
    public OffsetDateTime getCreateUTCTime() {
        return createUTCTime;
    }

    @Override
    public void setCreateUTCTime(OffsetDateTime createUTCTime) {
        this.createUTCTime = createUTCTime;
    }

    @Override
    public OffsetDateTime getLastModifiedUTCTime() {
        return lastModifiedUTCTime;
    }

    @Override
    public void setLastModifiedUTCTime(OffsetDateTime lastModifiedUTCTime) {
        this.lastModifiedUTCTime = lastModifiedUTCTime;
    }

    @Override
    public Long getLastModifiedTimestampMilli() {
        return lastModifiedTimestampMilli;
    }

    @Override
    public void setLastModifiedTimestampMilli(long lastModifiedTimestampMilli) {
        this.lastModifiedTimestampMilli = lastModifiedTimestampMilli;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ApiBaseEntity entity = (ApiBaseEntity) o;
        return getId() != null && Objects.equals(getId(), entity.getId());
    }
}
