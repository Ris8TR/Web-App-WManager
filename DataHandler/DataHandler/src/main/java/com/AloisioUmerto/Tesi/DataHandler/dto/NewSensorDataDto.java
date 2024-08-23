package com.AloisioUmerto.Tesi.DataHandler.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewSensorDataDto {
    private String sensorId;
    private LocalDateTime timestamp;
    private String payloadType;
    private double latitude;
    private double longitude;
    private Object payload;
    private String userId;
    private String interestAreaId;
}