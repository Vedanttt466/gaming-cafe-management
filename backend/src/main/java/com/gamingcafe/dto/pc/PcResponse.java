package com.gamingcafe.dto.pc;

import com.gamingcafe.entity.Pc;
import com.gamingcafe.entity.PcStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PcResponse {
    private Long id;
    private Integer pcNumber;
    private String specifications;
    private PcStatus status;
    private Long activeSessionId;
    private String occupantName;

    public static PcResponse fromEntity(Pc pc) {
        return PcResponse.builder()
                .id(pc.getId())
                .pcNumber(pc.getPcNumber())
                .specifications(pc.getSpecifications())
                .status(pc.getStatus())
                .build();
    }
}
