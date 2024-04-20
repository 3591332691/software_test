package com.demo.controller.admin;

import com.demo.entity.Venue;
import com.demo.service.VenueService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@RunWith(SpringRunner.class)
@WebMvcTest(AdminVenueController.class)
class AdminVenueControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VenueService venueService;
    @Test
    void test_venue_manage_page() throws Exception {
        List<Venue> venues = new ArrayList<>();
        venues.add(new Venue(1, "test1", "体育馆", 1000, "", "address", "08:00", "20:00"));
        venues.add(new Venue(2, "test2", "体育馆", 1000, "", "address", "08:00", "20:00"));

        // 设置Service层的行为
        when(venueService.findAll(PageRequest.of(0, 10, Sort.by("venueID").ascending())))
                .thenReturn(new PageImpl<>(venues));
        mockMvc.perform(get("/venue_manage"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/venue_manage"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("total"))
                .andExpect(MockMvcResultMatchers.model().attribute("total", 1))
                .andReturn();
    }

    @Test
    void testEditVenue_venueID_exists() throws Exception {
        Venue venue = new Venue(1, "test1", "体育馆", 1000, "", "address", "08:00", "20:00");
        when(venueService.findByVenueID(venue.getVenueID())).thenReturn(venue);
        MockHttpServletRequestBuilder requestBuilder = get("/venue_edit")
                .param("venueID", String.valueOf(venue.getVenueID()));
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("/admin/venue_edit"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("venue"))
                .andExpect(MockMvcResultMatchers.model().attribute("venue", venue))
                .andReturn();
    }

    @Test
    void testEditVenue_venueID_not_exists() throws Exception {
        int invalidVenueID = 1000;
        when(venueService.findByVenueID(invalidVenueID)).thenReturn(null);
        MockHttpServletRequestBuilder requestBuilder = get("/venue_edit")
                .param("venueID", String.valueOf(invalidVenueID));
        assertThrows(Exception.class, () ->
                mockMvc.perform(requestBuilder)
                        .andReturn());
    }

    @Test
    void test_venue_add_page() throws Exception{
        mockMvc.perform(get("/venue_add"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("/admin/venue_add"))
                .andReturn();
    }

    @Test
    void testGetVenueListJsonResponse_valid_page() throws Exception {
        int page = 1;
        List<Venue> venues = new ArrayList<>();
        venues.add(new Venue(1, "test1", "体育馆", 1000, "", "address", "08:00", "20:00"));
        venues.add(new Venue(2, "test2", "体育馆", 1000, "", "address", "08:00", "20:00"));

        when(venueService.findAll(PageRequest.of(page-1,10, Sort.by("venueID").ascending())))
                .thenReturn(new PageImpl<>(venues));
        this.mockMvc.perform(get("/venueList.do")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.hasKey("venueID")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]", Matchers.hasKey("venueName")))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].description", Matchers.hasItem(Matchers.containsString("体育"))))
                .andReturn();
    }

    @Test
    void testGetVenueListJsonResponse_empty_page() throws Exception {
        int page = 10;
        Page<Venue> emptyPage = Page.empty(Pageable.unpaged());
        when(venueService.findAll(PageRequest.of(page-1,10, Sort.by("venueID").ascending())))
                .thenReturn(emptyPage);
        this.mockMvc.perform(get("/venueList.do")
                        .param("page", String.valueOf(page))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty())
                .andReturn();
    }
    @Test
    void testGetVenueListJsonResponse_invalid_page() throws Exception {
        int page = 0;
        assertThrows(Exception.class, () ->
            venueService.findAll(PageRequest.of(page-1,10, Sort.by("venueID").ascending())));
    }

    @Test
    void testAddVenue_success() throws Exception {
        MockMultipartFile pictureFile = new MockMultipartFile("picture", new byte[]{});
        when(venueService.create(any())).thenReturn(1);
        MockHttpServletRequestBuilder requestBuilder = multipart("/addVenue.do")
                .file(pictureFile)
                .param("venueName", "test")
                .param("address", "上海市虹口区")
                .param("description", "足球场")
                .param("price", "600")
                .param("open_time", "08:00")
                .param("close_time", "20:00");
        this.mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("venue_manage"));
    }
    @Test
    void testAddVenue_lack_params_failure() throws Exception {
        MockMultipartFile pictureFile = new MockMultipartFile("picture", new byte[]{});
        when(venueService.create(any())).thenReturn(1);
        MockHttpServletRequestBuilder requestBuilder = multipart("/addVenue.do")
                .file(pictureFile);
        assertThrows(Exception.class, () ->
            this.mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("venue_manage"))
        );
    }
    @Test
    void testAddVenue_create_failure() throws Exception {
        MockMultipartFile pictureFile = new MockMultipartFile("picture", new byte[]{});
        when(venueService.create(any())).thenReturn(0);
        MockHttpServletRequestBuilder requestBuilder = multipart("/addVenue.do")
                .file(pictureFile)
                .param("venueName", "test")
                .param("address", "上海市虹口区")
                .param("description", "足球场")
                .param("price", "600")
                .param("open_time", "08:00")
                .param("close_time", "20:00");
        MvcResult result = this.mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("venue_add"))
                .andReturn();
        HttpServletRequest request = result.getRequest();
        assertEquals("添加失败！", request.getAttribute("message"));
    }
    @Test
    void testModifyVenue_success() throws Exception {
        Venue venue = new Venue(1, "test", "体育馆", 1000, "", "address", "08:00", "20:00");
        when(venueService.findByVenueID(venue.getVenueID())).thenReturn(venue);

        MockMultipartFile pictureFile = new MockMultipartFile("picture", new byte[]{});
        MockHttpServletRequestBuilder requestBuilder = multipart("/modifyVenue.do")
                .file(pictureFile)
                .param("venueID", String.valueOf(venue.getVenueID()))
                .param("venueName", "newName")
                .param("address", "上海市虹口区")
                .param("description", "体育馆")
                .param("price", "1000")
                .param("open_time", "08:00")
                .param("close_time", "20:00");

        this.mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("venue_manage"));

        verify(venueService, times(1)).findByVenueID(venue.getVenueID());
        verify(venueService, times(1)).update(venue);
        assertEquals("newName", venue.getVenueName());
        assertEquals("上海市虹口区", venue.getAddress());
    }
    @Test
    void testModifyVenue_lack_of_params() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/modifyVenue.do");
        assertThrows(Exception.class, () ->
            this.mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("venue_manage"))
        );
    }

    // 默认的venueService.delById(venueID)没有任何行为，只检查该方法被调用了一次
    @Test
    void test_del_Venue() throws Exception {
        int venueID = 1;
        mockMvc.perform(post("/delVenue.do").param("venueID", String.valueOf(venueID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        verify(venueService, times(1)).delById(venueID);
    }

    @Test
    void testCheckVenueName_venue_not_exist() throws Exception {
        String venueName = "不存在的场馆名";
        when(venueService.countVenueName(venueName)).thenReturn(0);
        mockMvc.perform(post("/checkVenueName.do").param("venueName", venueName))
                        .andExpect(MockMvcResultMatchers.content().string("true"));

        verify(venueService, times(1)).countVenueName(venueName);
    }
    @Test
    void testCheckVenueName_venue_exist() throws Exception {
        String venueName = "存在的场馆名";
        when(venueService.countVenueName(venueName)).thenReturn(1);
        mockMvc.perform(post("/checkVenueName.do").param("venueName", venueName))
                .andExpect(MockMvcResultMatchers.content().string("false"));

        verify(venueService, times(1)).countVenueName(venueName);
    }
}