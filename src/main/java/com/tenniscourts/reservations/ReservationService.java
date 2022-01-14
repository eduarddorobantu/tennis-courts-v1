package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestDTO;
import com.tenniscourts.guests.GuestService;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final GuestService guestService;

    private final ScheduleService scheduleService;

    private final BigDecimal RESERVATION_FEE = new BigDecimal(10);

    public List<ReservationDTO> getReservations(ReservationFilterRequestDTO reservationHistoryRequestDTO) {
        LocalDateTime startDateTime = reservationHistoryRequestDTO.getStartDateTime();
        LocalDateTime endDateTime = reservationHistoryRequestDTO.getEndDateTime();

        List<Reservation> filteredReservations =
                reservationRepository.findBySchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
                        startDateTime, endDateTime);

        return reservationMapper.map(filteredReservations);
    }

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        GuestDTO guestDTO = guestService.findById(createReservationRequestDTO.getGuestId());
        ScheduleDTO scheduleDTO = scheduleService.findSchedule(createReservationRequestDTO.getScheduleId());
        Guest guest = guestService.getGuestMapper().map(guestDTO);
        Schedule schedule = scheduleService.getScheduleMapper().map(scheduleDTO);

        checkReservationNotExists(schedule);
        checkScheduleIsBeforeCurrentTime(schedule);

        Reservation reservation = Reservation.builder()
                .guest(guest).schedule(schedule).reservationStatus(ReservationStatus.READY_TO_PLAY).value(RESERVATION_FEE)
                .build();

        return reservationMapper.map(reservationRepository.save(reservation));
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservationMapper::map).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(reservationId));
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long scheduleId) {
        Reservation previousReservation = reservationMapper.map(findReservation(previousReservationId));

        if (scheduleId.equals(previousReservation.getSchedule().getId())) {
            throw new IllegalArgumentException("Cannot reschedule to the same slot.");
        }

        reschedule(previousReservation);

        ReservationDTO newReservation = bookReservation(CreateReservationRequestDTO.builder()
                .guestId(previousReservation.getGuest().getId())
                .scheduleId(scheduleId)
                .build());
        newReservation.setPreviousReservation(reservationMapper.map(previousReservation));

        return newReservation;
    }

    public BigDecimal getRefundValue(Reservation reservation) {
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), reservation.getSchedule().getStartDateTime());

        if (hours >= 24) {
            return reservation.getValue();
        } else if (hours >= 12) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.75));
        } else if (hours >= 2) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.5));
        } else if (hours >= 0){
            return reservation.getValue().multiply(BigDecimal.valueOf(0.25));
        }

        return BigDecimal.ZERO;
    }

    private void checkScheduleIsBeforeCurrentTime(Schedule schedule) {
        if (schedule.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }

    private Reservation cancel(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {

            this.validateCancellation(reservation);

            BigDecimal refundValue = getRefundValue(reservation);
            return this.updateReservation(reservation, refundValue, ReservationStatus.CANCELLED);

        }).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        checkScheduleIsBeforeCurrentTime(reservation.getSchedule());
    }

    private void checkReservationNotExists(Schedule schedule) {
        List<Reservation> reservations = reservationRepository.findBySchedule_Id(schedule.getId());
        boolean reservationAlreadyExists = reservations.stream()
                .anyMatch(reservation -> ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus()));
        if (reservationAlreadyExists) {
            throw new AlreadyExistsEntityException("Reservation already exists for tennis court: " + schedule.getTennisCourt().getName() +
                    " for time: " + schedule.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
        }
    }

    private void reschedule(Reservation previousReservation) {
        validateCancellation(previousReservation);
        BigDecimal refundValue = getRefundValue(previousReservation);
        update(previousReservation, refundValue, ReservationStatus.RESCHEDULED);
    }

    private Reservation update(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }
}
