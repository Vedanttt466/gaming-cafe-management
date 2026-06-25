package com.gamingcafe.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeakHourPoint {
    private int hourOfDay; // 0-23
    private long sessionCount;
}
