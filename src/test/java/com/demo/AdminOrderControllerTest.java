package com.demo.controller.admin;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

import com.demo.entity.Order;
import com.demo.entity.vo.OrderVo;
import com.demo.service.OrderService;
import com.demo.service.OrderVoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AdminOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderVoService orderVoService;

    @InjectMocks
    private AdminOrderController adminOrderController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminOrderController).build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @Test
    public void testReservation_manage() throws Exception {
        // 模拟findAuditOrder()方法返回的订单列表
        List<Order> mockOrders = new ArrayList<>();
        mockOrders.add(new Order());
        // 设置orderService的行为
        when(orderService.findAuditOrder()).thenReturn(mockOrders);

        // 模拟returnVo()方法返回的订单视图列表
        List<OrderVo> mockOrderVos = new ArrayList<>();
        mockOrderVos.add(new OrderVo());
        // 设置orderVoService的行为
        when(orderVoService.returnVo(mockOrders)).thenReturn(mockOrderVos);

        // 模拟findNoAuditOrder()方法返回的未审核订单的总页数
        Page<Order> orderPage = new PageImpl<>(mockOrders);
        when(orderService.findNoAuditOrder(any())).thenReturn(orderPage);

        // 执行GET请求并验证响应
        mockMvc.perform(get("/reservation_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation_manage"))
                .andExpect(model().attributeExists("order_list"))
                .andExpect(model().attribute("order_list", mockOrderVos))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", mockOrders.size()));
    }

    @Test
    public void testGetNoAuditOrder() throws Exception {
        List<Order> mockOrders = new ArrayList<>();
        mockOrders.add(new Order());

        List<OrderVo> mockOrderVos = new ArrayList<>();
        mockOrderVos.add(new OrderVo());

        // 创建一个模拟的 Page 对象
        PageImpl<Order> mockPage = new PageImpl<>(mockOrders);

        // 模拟findNoAuditOrder()方法返回的未审核订单列表
        when(orderService.findNoAuditOrder(any(Pageable.class))).thenReturn(mockPage);
        when(orderVoService.returnVo(mockOrders)).thenReturn(mockOrderVos);

        // 模拟调用控制器端点
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/getOrderList.do")
                        .param("page", "1") // 传入参数
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 预期响应状态为 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 预期响应内容类型为 JSON
                .andExpect(jsonPath("$.length()").value(mockOrderVos.size())); // 预期返回的订单列表长度
    }

    @Test
    public void testConfirmOrder() throws Exception {
        // 模拟确认订单成功
        int mockOrderId = 123;

        // 设置orderService的行为
        doNothing().when(orderService).confirmOrder(mockOrderId);

        // 执行POST请求并验证响应
        mockMvc.perform(post("/passOrder.do").param("orderID", String.valueOf(mockOrderId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testConfirmOrderWithNonexistentOrder() throws Exception {
        // 模拟服务抛出异常
        doThrow(new RuntimeException("订单不存在")).when(orderService).confirmOrder(anyInt());

        // 发起请求并验证异常处理
        mockMvc.perform(post("/passOrder.do")
                        .param("orderID", "nonexistent_id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException));
    }

    @Test
    public void testRejectOrder() throws Exception {
        // 模拟拒绝订单成功
        int mockOrderId = 123;

        // 设置orderService的行为
        doNothing().when(orderService).rejectOrder(mockOrderId);

        // 执行POST请求并验证响应
        mockMvc.perform(post("/rejectOrder.do").param("orderID", String.valueOf(mockOrderId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testRejectOrderWithNonexistentOrder() throws Exception {
        // 模拟服务抛出异常
        doThrow(new RuntimeException("订单不存在")).when(orderService).confirmOrder(anyInt());

        // 发起请求并验证异常处理
        mockMvc.perform(post("/rejectOrder.do")
                        .param("orderID", "nonexistent_id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof RuntimeException));
    }
}