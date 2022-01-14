package com.tenniscourts.guests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenniscourts.TestConfigUtil;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GuestRemoteTest extends TestConfigUtil {

    private static final String GUEST_NAME_IN_DB = "Roger Federer";

    @Test
    @Transactional
    public void findAllGuests() throws Exception {
        MvcResult result = mvc.perform(get("/guests")).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        List<GuestDTO> guests = new ObjectMapper().readValue(json, List.class);

        assertEquals(2, guests.size());
    }

    @Test
    public void findGuestById() throws Exception {
        MvcResult result = mvc.perform(get("/guests/1")).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        GuestDTO actualGuestDTO = new ObjectMapper().readValue(json, GuestDTO.class);

        assertEquals(GUEST_NAME_IN_DB, actualGuestDTO.getName());
    }

    @Test
    public void findGuestById_NotFound() throws Exception {
        mvc.perform(get("/guests/10", 1)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void findGuestByName() throws Exception {
        MvcResult result = mvc.perform(get("/guests/guest").param("name", GUEST_NAME_IN_DB))
                .andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        GuestDTO actualGuestDTO = new ObjectMapper().readValue(json, GuestDTO.class);

        assertEquals(GUEST_NAME_IN_DB, actualGuestDTO.getName());
    }

    @Test
    @Transactional
    public void addGuest() throws Exception {
        GuestDTO expectedGuestDTO = new GuestDTO();
        expectedGuestDTO.setName("Mock Name");

        MvcResult result = mvc.perform(post("/guests").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(expectedGuestDTO))).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        GuestDTO actualGuestDTO = new ObjectMapper().readValue(json, GuestDTO.class);

        assertEquals(expectedGuestDTO.getName(), actualGuestDTO.getName());
    }

    @Test
    public void addGuest_NoName() throws Exception {
        GuestDTO expectedGuestDTO = new GuestDTO();

        mvc.perform(post("/guests").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(expectedGuestDTO))).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateGuest() throws Exception {
        GuestDTO expectedGuestDTO = new GuestDTO();
        expectedGuestDTO.setId(1L);
        expectedGuestDTO.setName("Mock Guest");

        MvcResult result = mvc.perform(put("/guests").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(expectedGuestDTO))).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        GuestDTO actualGuestDTO = new ObjectMapper().readValue(json, GuestDTO.class);

        assertEquals(expectedGuestDTO.getName(), actualGuestDTO.getName());
    }

    @Test
    @Transactional
    public void updateGuest_WrongId() throws Exception {
        GuestDTO expectedGuestDTO = new GuestDTO();
        expectedGuestDTO.setId(10L);
        expectedGuestDTO.setName("Mock Guest");

        mvc.perform(put("/guests").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(expectedGuestDTO))).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void deleteGuest() throws Exception {
        mvc.perform(delete("/guests/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mvc.perform(get("/guests/1")).andExpect(status().isNotFound());
    }

    @Test
    public void deleteGuest_WrongId() throws Exception {
        mvc.perform(delete("/guests/10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }
}