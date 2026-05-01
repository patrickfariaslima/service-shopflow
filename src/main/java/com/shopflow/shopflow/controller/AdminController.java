package com.shopflow.shopflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopflow.shopflow.dto.dashboard.DashBoardResponse;
import com.shopflow.shopflow.service.admin.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<DashBoardResponse> getDashBoard() {
        return ResponseEntity.ok(adminService.getDashBoard());
    }
    
}
