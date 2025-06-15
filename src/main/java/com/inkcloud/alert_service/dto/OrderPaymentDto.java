package com.inkcloud.alert_service.dto;

import java.time.LocalDateTime;

import com.inkcloud.alert_service.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderPaymentDto {
    private PaymentMethod method;
    private int price;
    private int count;

    private LocalDateTime at;
    private String pg;

}
