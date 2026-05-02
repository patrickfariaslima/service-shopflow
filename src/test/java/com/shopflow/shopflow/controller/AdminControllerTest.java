package com.shopflow.shopflow.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.shopflow.shopflow.dto.dashboard.DashBoardResponse;
import com.shopflow.shopflow.service.admin.AdminService;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.jwt.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getDashBoard_ShouldReturnDashBoardResponse() throws Exception {
        DashBoardResponse dashboard = DashBoardResponse.builder()
                .totalOrders(200L)
                .todayRevenue(BigDecimal.valueOf(50000))
                .lowStockProducts(List.of())
                .build();

        when(adminService.getDashBoard()).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(200))
                .andExpect(jsonPath("$.todayRevenue").value(50000));

        verify(adminService).getDashBoard();
    }
}
