package com.gamingcafe.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Used by staff to record a cash settlement for a final bill. */
@Data
public class CashPaymentRequest {

    @NotNull
    private Long sessionId;
}
