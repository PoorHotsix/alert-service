package com.inkcloud.alert_service.service;

import com.inkcloud.alert_service.dto.OrderDto;

public interface AlertMainService {
    abstract void sendOrderCompleteEmail(OrderDto orderDto);
}
