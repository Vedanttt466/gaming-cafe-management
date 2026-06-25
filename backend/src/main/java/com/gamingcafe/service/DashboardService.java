package com.gamingcafe.service;

import com.gamingcafe.dto.dashboard.*;
import com.gamingcafe.entity.*;
import com.gamingcafe.repository.BookingRepository;
import com.gamingcafe.repository.PcRepository;
import com.gamingcafe.repository.SessionRepository;
import com.gamingcafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PcRepository pcRepository;
    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public DashboardSummaryResponse getSummary() {
        List<Pc> allPcs = pcRepository.findAll();
        Map<PcStatus, Long> pcCounts = allPcs.stream()
                .collect(Collectors.groupingBy(Pc::getStatus, Collectors.counting()));

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal todayRevenue = sessionRepository.sumGrossRevenueBetween(startOfToday, now);
        BigDecimal weekRevenue = sessionRepository.sumGrossRevenueBetween(startOfWeek, now);
        BigDecimal monthRevenue = sessionRepository.sumGrossRevenueBetween(startOfMonth, now);

        long todaySessions = sessionRepository.countByStatusAndStartTimeBetween(
                SessionStatus.COMPLETED, startOfToday, now)
                + sessionRepository.findByStatus(SessionStatus.ACTIVE).size();

        long todayNoShows = bookingRepository.countByStatusAndCreatedAtBetween(
                BookingStatus.NO_SHOW, startOfToday, now);

        BigDecimal forfeitedToday = BigDecimal.valueOf(todayNoShows).multiply(BigDecimal.valueOf(50));

        return DashboardSummaryResponse.builder()
                .totalPcs(allPcs.size())
                .availablePcs(pcCounts.getOrDefault(PcStatus.AVAILABLE, 0L).intValue())
                .occupiedPcs(pcCounts.getOrDefault(PcStatus.OCCUPIED, 0L).intValue())
                .reservedPcs(pcCounts.getOrDefault(PcStatus.RESERVED, 0L).intValue())
                .maintenancePcs(pcCounts.getOrDefault(PcStatus.MAINTENANCE, 0L).intValue())
                .activeSessions(sessionRepository.findByStatus(SessionStatus.ACTIVE).size())
                .pendingReservations(bookingRepository.findByStatusIn(
                        List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED)).size())
                .todayRevenue(todayRevenue)
                .weekRevenue(weekRevenue)
                .monthRevenue(monthRevenue)
                .todaySessionsCount(todaySessions)
                .todayNoShowCount(todayNoShows)
                .todayForfeitedTokens(forfeitedToday)
                .build();
    }

    /** Daily revenue trend for the last N days - used for revenue charts. */
    public List<RevenueTrendPoint> getRevenueTrend(int days) {
        List<RevenueTrendPoint> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.plusDays(1).atStartOfDay();

            BigDecimal revenue = sessionRepository.sumGrossRevenueBetween(from, to);
            long count = sessionRepository.findCompletedBetween(from, to).size();

            points.add(RevenueTrendPoint.builder()
                    .label(date.format(fmt))
                    .revenue(revenue)
                    .sessionCount(count)
                    .build());
        }
        return points;
    }

    /** Peak-hour analysis: number of sessions started in each hour of the day, last 30 days. */
    public List<PeakHourPoint> getPeakHours() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();

        List<Session> sessions = sessionRepository.findCompletedBetween(from, to);
        Map<Integer, Long> hourCounts = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getStartTime().getHour(), Collectors.counting()));

        List<PeakHourPoint> result = new ArrayList<>();
        for (int hour = 8; hour <= 22; hour++) {
            result.add(PeakHourPoint.builder()
                    .hourOfDay(hour)
                    .sessionCount(hourCounts.getOrDefault(hour, 0L))
                    .build());
        }
        return result;
    }

    public List<CustomerHistoryResponse> getCustomerHistories() {
        List<User> customers = userRepository.findByRole(Role.CUSTOMER,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        return customers.stream().map(customer -> {
            long visits = sessionRepository.countByCustomerId(customer.getId());
            long noShows = bookingRepository.findByCustomerIdOrderByBookingTimeDesc(
                            customer.getId(), org.springframework.data.domain.Pageable.unpaged())
                    .getContent().stream()
                    .filter(b -> b.getStatus() == BookingStatus.NO_SHOW)
                    .count();

            List<Session> history = sessionRepository.findByCustomerIdOrderByStartTimeDesc(
                    customer.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();

            BigDecimal totalSpend = history.stream()
                    .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                    .map(s -> s.getGrossAmount() == null ? BigDecimal.ZERO : s.getGrossAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDateTime lastVisit = history.stream()
                    .map(Session::getStartTime)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            return CustomerHistoryResponse.builder()
                    .customerId(customer.getId())
                    .name(customer.getName())
                    .email(customer.getEmail())
                    .phone(customer.getPhone())
                    .totalVisits(visits)
                    .noShowCount(noShows)
                    .totalSpend(totalSpend)
                    .lastVisit(lastVisit)
                    .build();
        }).collect(Collectors.toList());
    }
}
