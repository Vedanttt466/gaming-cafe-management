package com.gamingcafe.controller;

import com.gamingcafe.dto.auth.CreateStaffRequest;
import com.gamingcafe.dto.common.ApiResponse;
import com.gamingcafe.entity.User;
import com.gamingcafe.service.AuthService;
import com.gamingcafe.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class StaffManagementController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        User staff = authService.createStaff(request);
        return ResponseEntity.ok(ApiResponse.success("Staff account created", staff.getEmail()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> listStaff(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.listStaff(PageRequest.of(page, size))));
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<ApiResponse<String>> toggleEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.toggleEnabled(id, enabled);
        return ResponseEntity.ok(ApiResponse.success("Staff account updated", enabled ? "ENABLED" : "DISABLED"));
    }
}
