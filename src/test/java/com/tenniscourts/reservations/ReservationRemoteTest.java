package com.tenniscourts.reservations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenniscourts.TestConfigUtil;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReservationRemoteTest extends TestConfigUtil {

    @Test
    public void retrievePastReservations() throws Exception {
        ReservationFilterRequestDTO reservationFilterRequestDTO = new ReservationFilterRequestDTO();
        reservationFilterRequestDTO.setStartDateTime(LocalDateTime.now().minusYears(50));
        reservationFilterRequestDTO.setEndDateTime(LocalDateTime.now());

        MvcResult result = mvc.perform(post("/reservations/filters").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reservationFilterRequestDTO))).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDTO> reservations = new ObjectMapper().readValue(json, List.class);

        assertEquals(0, reservations.size());
    }

    @Test
    public void retrieveReservationsBetween() throws Exception {
        ReservationFilterRequestDTO reservationFilterRequestDTO = new ReservationFilterRequestDTO();
        reservationFilterRequestDTO.setStartDateTime(LocalDateTime.now().minusYears(50));
        reservationFilterRequestDTO.setEndDateTime(LocalDateTime.now().plusYears(50));

        MvcResult result = mvc.perform(post("/reservations/filters").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(reservationFilterRequestDTO))).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        List<ReservationDTO> reservations = new ObjectMapper().readValue(json, List.class);

        assertEquals(3, reservations.size());
    }

    @Test
    @Transactional
    public void bookReservation() throws Exception {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(1L);

        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReservationRequestDTO))).andExpect(status().isCreated());
    }

    @Test
    @Transactional
    public void bookReservation_WrongGuestId() throws Exception {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(10L);
        createReservationRequestDTO.setScheduleId(1L);

        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReservationRequestDTO))).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void bookReservation_ScheduleIdNotInDB() throws Exception {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(10L);

        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReservationRequestDTO))).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void bookReservation_ScheduleSlotAlreadyTaken() throws Exception {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(2L);

        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReservationRequestDTO))).andExpect(status().isConflict());
    }

    @Test
    @Transactional
    public void bookReservation_ScheduleInThePast() throws Exception {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(3L);

        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createReservationRequestDTO))).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void cancelReservation() throws Exception {
        MvcResult result = mvc.perform(delete("/reservations/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        ReservationDTO reservationDTO = new ObjectMapper().readValue(json, ReservationDTO.class);

        assertNotNull(reservationDTO);
        assertTrue(reservationDTO.getId() == 1);
    }

    @Test
    @Transactional
    public void cancelReservation_WrongStatus() throws Exception {
        mvc.perform(delete("/reservations/2").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void rescheduleReservationOtherSlot() throws Exception {
        mvc.perform(put("/reservations/1/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void rescheduleReservationSameSlot() throws Exception {
        mvc.perform(put("/reservations/1/2").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }
}