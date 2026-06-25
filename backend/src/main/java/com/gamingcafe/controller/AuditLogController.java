package com.gamingcafe.controller;

import com.gamingcafe.dto.common.ApiResponse;
import com.gamingcafe.entity.AuditLog;
import com.gamingcafe.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLog>>> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        Page<AuditLog> logs = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
