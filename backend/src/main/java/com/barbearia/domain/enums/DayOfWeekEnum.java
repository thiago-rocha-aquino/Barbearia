package com.barbearia.domain.enums;

import java.time.DayOfWeek;

public enum DayOfWeekEnum {
    MONDAY(DayOfWeek.MONDAY),
    TUESDAY(DayOfWeek.TUESDAY),
    WEDNESDAY(DayOfWeek.WEDNESDAY),
    THURSDAY(DayOfWeek.THURSDAY),
    FRIDAY(DayOfWeek.FRIDAY),
    SATURDAY(DayOfWeek.SATURDAY),
    SUNDAY(DayOfWeek.SUNDAY);

    private final DayOfWeek javaDayOfWeek;

    DayOfWeekEnum(DayOfWeek javaDayOfWeek) {
        this.javaDayOfWeek = javaDayOfWeek;
    }

    public DayOfWeek toJavaDayOfWeek() {
        return javaDayOfWeek;
    }

    public static DayOfWeekEnum fromJavaDayOfWeek(DayOfWeek dayOfWeek) {
        for (DayOfWeekEnum value : values()) {
            if (value.javaDayOfWeek == dayOfWeek) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
    }
}
