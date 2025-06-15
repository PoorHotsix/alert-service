package com.inkcloud.alert_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderShipDto {
    private String name;
    private String receiver;
    private int zipcode;

    private String addressMain;
    private String addressSub;
    private String contact;
}
