package com.mehmetozanguven.inghubs_digital_wallet.core.commonModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
@SuperBuilder
public abstract class ApiBaseModel implements Serializable {

    private String id;
    @Setter(value = AccessLevel.PRIVATE)
    private Long createTimestampMilli;
    @Setter(value = AccessLevel.PRIVATE)
    private OffsetDateTime createUTCTime;
    @Setter(value = AccessLevel.PRIVATE)
    private OffsetDateTime lastModifiedUTCTime;
    @Setter(value = AccessLevel.PRIVATE)
    private Long lastModifiedTimestampMilli;
    @Setter(value = AccessLevel.PRIVATE)
    private Long entityVersionId;
}
