package com.lc.finalexam.controller;

import com.lc.finalexam.entity.User;
import com.lc.finalexam.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
class AdminOrderTemplateRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void orderListLinksAStylesheetThatCanBeServed() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());
        when(orderService.getAdminOrderStatistics()).thenReturn(Map.of(
                "total", 0L,
                "pending", 0L,
                "paid", 0L,
                "shipped", 0L,
                "completed", 0L,
                "cancelled", 0L));

        mockMvc.perform(get("/admin/order/list")
                        .sessionAttr("admin", new User()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "/css/AdminOrderListStyle.css")));

        mockMvc.perform(get("/css/AdminOrderListStyle.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(".order-table")));
    }

    @Test
    void orderDetailStylesheetCanBeServed() throws Exception {
        mockMvc.perform(get("/css/AdminOrderDetailStyle.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(".order-info-card")));
    }
}
