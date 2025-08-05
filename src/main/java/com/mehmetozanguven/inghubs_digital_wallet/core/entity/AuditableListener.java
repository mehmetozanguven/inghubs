package com.mehmetozanguven.inghubs_digital_wallet.core.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class AuditableListener {

    @PrePersist
    void preCreate(Auditable auditable) {
        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.UTC);
        auditable.setCreateUTCTime(now);
        auditable.setCreateTimestampMilli(now.toInstant().toEpochMilli());
        auditable.setLastModifiedUTCTime(now);
        auditable.setLastModifiedTimestampMilli(now.toInstant().toEpochMilli());
    }

    @PreUpdate
    void preUpdate(Auditable auditable) {
        OffsetDateTime now = Instant.now().atOffset(ZoneOffset.UTC);
        auditable.setLastModifiedUTCTime(now);
        auditable.setLastModifiedTimestampMilli(now.toInstant().toEpochMilli());
    }
}
