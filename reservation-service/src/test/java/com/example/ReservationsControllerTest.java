package com.example;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest
public class ReservationsControllerTest {

    @MockBean ReservationRepository reservations;

    @Autowired MockMvc mvc;

    @Test
    public void should_not_get_not_existing_reservation() throws Exception {
        // when: MockMvcRequestBuilders...
        mvc.perform(get("/custom-reservations/Krzysiek"))
            // MockMvcResultHandlers...
            .andDo(print())

            // then: MockMvcResultMatchers...
            .andExpect(status().isNotFound());
    }

    @Test
    public void should_get_existing_reservation() throws Exception {
        // given: Mockito...
        when(reservations.findByName("Krzysiek"))
            .thenReturn(new Reservation(5L, "Krzysiek"));

        // when: MockMvcRequestBuilders...
        mvc.perform(get("/custom-reservations/Krzysiek"))
            // MockMvcResultHandlers...
            .andDo(print())

        // then: MockMvcResultMatchers...
            .andExpect(status().isOk())
            .andExpect(jsonPath("@.id").value("5"))
            .andExpect(jsonPath("@.name").value("Krzysiek"));
    }
}
