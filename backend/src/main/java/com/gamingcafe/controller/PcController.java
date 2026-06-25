package com.gamingcafe.controller;

import com.gamingcafe.dto.common.ApiResponse;
import com.gamingcafe.dto.pc.PcResponse;
import com.gamingcafe.entity.Pc;
import com.gamingcafe.service.PcService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pcs")
@RequiredArgsConstructor
public class PcController {

    private final PcService pcService;

    /** Public endpoint - shown on the customer landing page / kiosk display. */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<PcResponse>>> getStatus() {
        List<PcResponse> response = pcService.getAll().stream().map(PcResponse::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/maintenance")
    @PreAuthorize("hasAnyRole('STAFF','OWNER')")
    public ResponseEntity<ApiResponse<PcResponse>> setMaintenance(
            @PathVariable Long id, @RequestParam boolean underMaintenance) {
        Pc pc = pcService.setMaintenance(id, underMaintenance);
        return ResponseEntity.ok(ApiResponse.success("PC status updated", PcResponse.fromEntity(pc)));
    }
}
