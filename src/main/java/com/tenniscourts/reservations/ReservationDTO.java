package com.tenniscourts.reservations;

import com.tenniscourts.guests.GuestDTO;
import com.tenniscourts.schedules.ScheduleDTO;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReservationDTO {

    private Long id;

    private GuestDTO guest;

    private ScheduleDTO schedule;

    private String reservationStatus;

    private ReservationDTO previousReservation;

    private BigDecimal refundValue;

    private BigDecimal value;

    @NotNull
    private Long scheduledId;

    @NotNull
    private Long guestId;
}
