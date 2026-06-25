package com.gamingcafe.websocket;

import com.gamingcafe.dto.pc.PcResponse;
import com.gamingcafe.entity.Pc;
import com.gamingcafe.repository.PcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pushes real-time PC status updates to all connected dashboard clients
 * whenever a PC's status changes (session start/end, reservation, expiry, maintenance).
 */
@Component
@RequiredArgsConstructor
public class PcStatusBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final PcRepository pcRepository;

    public void broadcastAllPcStatuses() {
        List<PcResponse> statuses = pcRepository.findAllByOrderByPcNumberAsc()
                .stream()
                .map(PcResponse::fromEntity)
                .toList();
        messagingTemplate.convertAndSend("/topic/pc-status", statuses);
    }

    public void broadcastPcStatus(Pc pc) {
        messagingTemplate.convertAndSend("/topic/pc-status/" + pc.getId(), PcResponse.fromEntity(pc));
        broadcastAllPcStatuses();
    }

    public void notifySessionsChanged() {
        messagingTemplate.convertAndSend("/topic/sessions", "REFRESH");
    }

    public void notifyDashboardChanged() {
        messagingTemplate.convertAndSend("/topic/dashboard", "REFRESH");
    }
}
