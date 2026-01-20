package com.barbearia.domain.entity;

import com.barbearia.domain.enums.DayOfWeekEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "working_hours", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"barber_id", "day_of_week"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingHours extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private User barber;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeekEnum dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_working", nullable = false)
    @Builder.Default
    private boolean isWorking = true;

    public boolean isWithinWorkingHours(LocalTime time) {
        if (!isWorking) {
            return false;
        }
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }
}
