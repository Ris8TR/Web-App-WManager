package com.AloisioUmerto.Tesi.DataHandler.dto;

import lombok.Data;

@Data
public class NewSensorDataDto {
    private String sensorId;
    private String payloadType;
    private Object payload;
    private double latitude;
    private double longitude;
}