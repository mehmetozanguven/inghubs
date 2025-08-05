package com.mehmetozanguven.inghubs_digital_wallet.core;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DateOperation {
    public static OffsetDateTime getOffsetNowAsUTC() {
        return Instant.now().atOffset(ZoneOffset.UTC);
    }
    public static OffsetDateTime addDurationToUTCNow(Duration duration) {
        return addDuration(getOffsetNowAsUTC(), duration);
    }

    public static OffsetDateTime addDuration(OffsetDateTime givenDate, Duration duration) {
        return givenDate.plus(duration);
    }

    public static OffsetDateTime minusDuration(OffsetDateTime givenDate, Duration duration) {
        return givenDate.minus(duration);
    }
}
