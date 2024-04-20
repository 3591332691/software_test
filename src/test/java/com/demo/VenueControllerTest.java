package com.demo;

import com.demo.controller.user.VenueController;
import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@RunWith(SpringRunner.class)
@WebMvcTest(VenueController.class)
class VenueControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VenueService venueService;
    @Test
    void testToGymPage_VenueID_Exists() throws Exception {
        Venue venue = new Venue(1, "test", "体育馆", 1000, "", "address", "08:00", "20:00");
        when(venueService.findByVenueID(venue.getVenueID())).thenReturn(venue);
        MockHttpServletRequestBuilder requestBuilder = get("/venue")
                .param("venueID", String.valueOf(venue.getVenueID()));
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("venue"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("venue"))
                .andExpect(MockMvcResultMatchers.model().attribute("venue", venue))
                .andReturn();
    }

    @Test
    void testToGymPage_VenueID_NotExists() throws Exception {
        int invalidVenueID = 1000;
        when(venueService.findByVenueID(invalidVenueID)).thenReturn(null);
        MockHttpServletRequestBuilder requestBuilder = get("/venue")
                .param("venueID", String.valueOf(invalidVenueID));
        assertThrows(Exception.class, () ->
                mockMvc.perform(requestBuilder)
                        .andReturn());
    }
    @Test
    void testVenueList() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(new Venue(1, "test1", "体育馆", 1000, "", "address", "08:00", "20:00"));
        venues.add(new Venue(2, "test2", "体育馆", 1000, "", "address", "08:00", "20:00"));

        Pageable venue_pageable= PageRequest.of(0,5, Sort.by("venueID").ascending());
        when(venueService.findAll(venue_pageable)).thenReturn(new PageImpl<>(venues));
        this.mockMvc.perform(get("/venue_list"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("venue_list"))
                .andExpect(MockMvcResultMatchers.model().attribute("venue_list", venues))
                .andExpect(MockMvcResultMatchers.model().attribute("total", 1))
                .andReturn();
    }
    @Test
    void testVenueListJsonResponse() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(new Venue(1, "test1", "体育馆", 1000, "", "address", "08:00", "20:00"));
        venues.add(new Venue(2, "test2", "体育馆", 1000, "", "address", "08:00", "20:00"));
        when(venueService.findAll(any())).thenReturn(new PageImpl<>(venues));

        this.mockMvc.perform(get("/venuelist/getVenueList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].description", Matchers.hasItem(Matchers.containsString("体育"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(venues.size()))
                .andReturn();
    }
}