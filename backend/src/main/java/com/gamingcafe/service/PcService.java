package com.gamingcafe.service;

import com.gamingcafe.entity.Booking;
import com.gamingcafe.entity.BookingStatus;
import com.gamingcafe.entity.Pc;
import com.gamingcafe.entity.PcStatus;
import com.gamingcafe.exception.NoPcAvailableException;
import com.gamingcafe.exception.ResourceNotFoundException;
import com.gamingcafe.repository.BookingRepository;
import com.gamingcafe.repository.PcRepository;
import com.gamingcafe.websocket.PcStatusBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PcService {

    private final PcRepository pcRepository;
    private final BookingRepository bookingRepository;
    private final PcStatusBroadcaster broadcaster;

    public List<Pc> getAll() {
        return pcRepository.findAllByOrderByPcNumberAsc();
    }

    public Pc getById(Long id) {
        return pcRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PC not found with id: " + id));
    }

    /**
     * Finds and reserves a PC for a new walk-in session.
     * A PC qualifies if its status is AVAILABLE.
     */
    @Transactional
    public Pc assignPcForWalkIn() {
        List<Pc> available = pcRepository.findByStatus(PcStatus.AVAILABLE);
        if (available.isEmpty()) {
            throw new NoPcAvailableException("All 10 PCs are currently occupied. Please wait for a PC to free up.");
        }
        Pc pc = available.get(0);
        pc.setStatus(PcStatus.OCCUPIED);
        Pc saved = pcRepository.save(pc);
        broadcaster.broadcastPcStatus(saved);
        return saved;
    }

    /**
     * Finds and tentatively reserves a PC for a new online booking at the requested time.
     * A PC qualifies if it has no overlapping CONFIRMED booking for the 30-minute hold window
     * and is not permanently set to MAINTENANCE.
     */
    @Transactional
    public Pc assignPcForBooking(LocalDateTime windowStart, LocalDateTime windowEnd) {
        List<Pc> candidatePcs = pcRepository.findAllByOrderByPcNumberAsc().stream()
                .filter(pc -> pc.getStatus() != PcStatus.MAINTENANCE)
                .collect(Collectors.toList());

        if (candidatePcs.isEmpty()) {
            throw new NoPcAvailableException("No PCs are currently available for booking.");
        }

        Set<Long> busyPcIds = candidatePcs.stream()
                .filter(pc -> !bookingRepository.findOverlappingActiveBookingsForPc(
                                pc.getId(), BookingStatus.CONFIRMED, windowStart, windowEnd).isEmpty()
                        || pc.getStatus() == PcStatus.OCCUPIED)
                .map(Pc::getId)
                .collect(Collectors.toSet());

        Pc chosen = candidatePcs.stream()
                .filter(pc -> !busyPcIds.contains(pc.getId()))
                .findFirst()
                .orElseThrow(() -> new NoPcAvailableException(
                        "All 10 PCs are already reserved or occupied for this time slot. Please choose another time."));

        chosen.setStatus(PcStatus.RESERVED);
        Pc saved = pcRepository.save(chosen);
        broadcaster.broadcastPcStatus(saved);
        return saved;
    }

    @Transactional
    public void releasePc(Pc pc) {
        pc.setStatus(PcStatus.AVAILABLE);
        Pc saved = pcRepository.save(pc);
        broadcaster.broadcastPcStatus(saved);
    }

    @Transactional
    public void markOccupied(Pc pc) {
        pc.setStatus(PcStatus.OCCUPIED);
        Pc saved = pcRepository.save(pc);
        broadcaster.broadcastPcStatus(saved);
    }

    @Transactional
    public Pc setMaintenance(Long pcId, boolean underMaintenance) {
        Pc pc = getById(pcId);
        pc.setStatus(underMaintenance ? PcStatus.MAINTENANCE : PcStatus.AVAILABLE);
        Pc saved = pcRepository.save(pc);
        broadcaster.broadcastPcStatus(saved);
        return saved;
    }
}
