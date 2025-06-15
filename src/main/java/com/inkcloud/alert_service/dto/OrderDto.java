package com.inkcloud.alert_service.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.inkcloud.alert_service.enums.OrderState;
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
public class OrderDto {
    private String id; // ㅇ
    private OrderState state; // ㅇ
    private LocalDateTime createdAt; // ㅇ
    
    private int price; // ㅇ
    private int quantity; // ㅇ 
    private PaymentMethod method;
    
    private OrderMemberDto member; // ㅇ 

    private List<OrderItemDto> orderItems; // ㅇ
    private OrderShipDto orderShip; // ㅇ
}
