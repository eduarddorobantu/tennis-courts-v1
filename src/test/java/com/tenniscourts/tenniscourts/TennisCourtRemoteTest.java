package com.tenniscourts.tenniscourts;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static junit.framework.TestCase.assertTrue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TennisCourtRemoteTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void findTennisCourtById() throws Exception {
        MvcResult result = mvc.perform(get("/tennis-courts/1")).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        TennisCourtDTO tennisCourt = new ObjectMapper().readValue(json, TennisCourtDTO.class);

        assertTrue(tennisCourt != null);
    }

    @Test
    public void findTennisCourtById_NotFound() throws Exception {
        mvc.perform(get("/tennis-courts/10")).andExpect(status().isNotFound());
    }

    @Test
    public void findTennisCourtWithSchedulesById() throws Exception {
        MvcResult result = mvc.perform(get("/tennis-courts/1/schedules")).andExpect(status().isOk()).andReturn();

        String json = result.getResponse().getContentAsString();
        TennisCourtDTO tennisCourt = new ObjectMapper().readValue(json, TennisCourtDTO.class);

        assertTrue(tennisCourt != null);
    }

    @Test
    public void findTennisCourtWithSchedulesById_NotFound() throws Exception {
        mvc.perform(get("/tennis-courts/10/schedules")).andExpect(status().isNotFound());
    }

    @Test
    public void addTennisCourtTestBadRequest() throws Exception {
        mvc.perform(post("/tennis-courts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addTennisCourtTest() throws Exception {
        TennisCourtDTO tennisCourtDTO = TennisCourtDTO.builder().name("Roland Garros").build();

        mvc.perform(post("/tennis-courts", tennisCourtDTO).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(tennisCourtDTO)))
                .andExpect(status().isCreated());
    }

}
