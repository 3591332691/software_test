package com.demo.controller.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import com.demo.dao.VenueDao;
import com.demo.entity.Order;
import com.demo.entity.User;
import com.demo.entity.Venue;
import com.demo.entity.vo.OrderVo;
import com.demo.exception.LoginException;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import com.demo.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderVoService orderVoService;

    @MockBean
    private VenueService venueService;

    @MockBean
    private VenueDao venueDao;

    @Test
    public void testOrderManage() throws Exception {
        // 模拟 HttpSession 中存储的用户对象
        User loginUser = new User();
        loginUser.setUserID("123");
        MockHttpSession session = mock(MockHttpSession.class);
        when(session.getAttribute("user")).thenReturn(loginUser);

        List<Order> mockOrders = new ArrayList<>();
        mockOrders.add(new Order());

        Page<Order> mockPage = new PageImpl<>(mockOrders);
        when(orderService.findUserOrder(eq(loginUser.getUserID()), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/order_manage").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("order_manage"));

        // 验证 MockHttpSession 中的 "user" 属性是否被获取
        verify(session, atLeastOnce()).getAttribute("user");
    }

    @Test
    public void testOrderManageNoLogin() throws Exception {
        // 模拟 HttpSession 中存储的用户对象
        MockHttpSession session = mock(MockHttpSession.class);
        when(session.getAttribute("user")).thenReturn(null);

        assertThrows(Exception.class, () -> mockMvc.perform(get("/order_manage").session(session)));

    }

    @Test
    public void testOrderPlace() throws Exception {
        int venueID = 456; // 假设场馆ID为456
        Venue mockVenue = new Venue();
        when(venueService.findByVenueID(venueID)).thenReturn(mockVenue);

        // 发起 GET 请求并验证返回结果
        mockMvc.perform(get("/order_place.do").param("venueID", String.valueOf(venueID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", mockVenue)) // 验证Model中的"venue"属性值与mockVenue相等
                .andExpect(view().name("order_place"));
    }

    @Test
    public void testOrderPlaceVenueNotFound() throws Exception {
        int venueID = 123; // 假设不存在的场馆ID
        when(venueService.findByVenueID(venueID)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/order_place.do")
                        .param("venueID", String.valueOf(venueID)))
                .andExpect(status().isFound());
    }

    @Test
    public void testOrderList() throws Exception {
        // 模拟用户登录
        User loginUser = new User();
        loginUser.setUserID("123");
        MockHttpSession session = mock(MockHttpSession.class);
        when(session.getAttribute("user")).thenReturn(loginUser);

        List<OrderVo> orderVos = new ArrayList<>();
        orderVos.add(new OrderVo());
        when(orderVoService.returnVo(any(List.class))).thenReturn(orderVos);

        List<Order> mockOrders = new ArrayList<>();
        mockOrders.add(new Order());
        when(orderService.findUserOrder(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(mockOrders));

        mockMvc.perform(get("/getOrderList.do").param("page", "1").session(session))
                .andExpect(status().isOk()) // 验证HTTP状态码为200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 验证返回的内容类型为JSON
                .andExpect(jsonPath("$.length()").value(orderVos.size()));
    }

    @Test
    public void testOrderListNoLogin() throws Exception {
        MockHttpSession session = mock(MockHttpSession.class);
        when(session.getAttribute("user")).thenReturn(null);

        assertThrows(Exception.class, () -> mockMvc.perform(get("/getOrderList.do").param("page", "1").session(session)));
    }

    @Test
    public void testAddOrder() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setUserID("123");
        session.setAttribute("user", user);

        String venueName = "VenueName";
        String date = "2024-04-16";
        String startTime = "12:00";
        int hours = 2;

        mockMvc.perform(post("/addOrder.do")
                        .param("venueName", venueName)
                        .param("date", date)
                        .param("startTime", date + " " + startTime)
                        .param("hours", String.valueOf(hours))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));
    }

    @Test
    public void testAddOrderWithErrorInput() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = new User();
        user.setUserID("123");
        session.setAttribute("user", user);

        String venueName = "VenueName";
        String date = "2024-04-16";
        String startTime = "12:00";
        int hours = 2;
        when(venueService.findByVenueName(venueName)).thenReturn(null);
        assertThrows(Exception.class, () -> mockMvc.perform(get("/addOrder.do").param("venueName", venueName)
                .param("date", date)
                .param("startTime", date + " " + startTime)
                .param("hours", String.valueOf(hours))
                .session(session)));
    }

    @Test
    public void testFinishOrder() throws Exception {
        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testFinishOrderInvalidId() throws Exception {
        doThrow(new RuntimeException("订单不存在")).when(orderService).finishOrder(anyInt());

        mockMvc.perform(post("/finishOrder.do")
                        .param("orderID", "nonexistent_id"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException));
    }

    @Test
    public void testEditOrder() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setVenueID(1);

        Venue mockVenue = new Venue();
        mockVenue.setVenueName("Venue");

        when(orderService.findById(1)).thenReturn(mockOrder);
        when(venueService.findByVenueID(1)).thenReturn(mockVenue);

        // Perform GET request
        mockMvc.perform(get("/modifyOrder.do")
                        .param("orderID", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", mockOrder))
                .andExpect(model().attributeExists("venue"))
                .andExpect(model().attribute("venue", mockVenue))
                .andExpect(view().name("order_edit"));
    }

    @Test
    public void testModifyOrder() throws Exception {
        String venueName = "Mock Venue";
        String date = "2024-04-16";
        String startTime = "10:00";
        int hours = 2;
        int orderID = 1;

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new User());

        mockMvc.perform(post("/modifyOrder")
                        .param("venueName", venueName)
                        .param("date", date)
                        .param("startTime", date + " " + startTime)
                        .param("hours", String.valueOf(hours))
                        .param("orderID", String.valueOf(orderID))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("order_manage"));
    }

    @Test
    public void testModifyOrderWithErrorInput() throws Exception {
        String venueName = "Mock Venue";
        String date = "2024-04-16";
        String startTime = "10:00";
        int hours = 2;
        int orderID = 1;

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", new User());

        when(venueService.findByVenueName(venueName)).thenReturn(null);
        assertThrows(Exception.class, () -> mockMvc.perform(post("/modifyOrder")
                .param("venueName", venueName)
                .param("date", date)
                .param("startTime", date + " " + startTime)
                .param("hours", String.valueOf(hours))
                .param("orderID", String.valueOf(orderID))
                .session(session)));
    }

    @Test
    public void testDelOrder() throws Exception {
        int orderID = 1;

        mockMvc.perform(post("/delOrder.do")
                        .param("orderID", String.valueOf(orderID)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testGetOrder() throws Exception {
        String venueName = "Mock Venue";
        String date = "2024-04-16";

        Venue mockVenue = new Venue();
        mockVenue.setVenueID(1);
        when(venueService.findByVenueName(venueName)).thenReturn(mockVenue);

        List<Order> mockOrderList = new ArrayList<>();
        Order order = new Order();
        order.setOrderID(111);
        mockOrderList.add(order);
        when(orderService.findDateOrder(eq(1), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockOrderList);

        mockMvc.perform(get("/order/getOrderList.do")
                        .param("venueName", venueName)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.venue.venueID").value(mockVenue.getVenueID()))// Check venue ID in response JSON
                .andExpect(jsonPath("$.orders[0].orderID").value(order.getOrderID())); // Check size of orders list in response JSON
    }

    @Test
    public void testGetOrderNoExist() throws Exception {
        String venueName = "Mock Venue";
        String date = "2024-04-16";

        when(venueService.findByVenueName(venueName)).thenReturn(null);

        assertThrows(Exception.class, () -> mockMvc.perform(get("/order/getOrderList.do")
                .param("venueName", venueName)
                .param("date", date)));
    }
}