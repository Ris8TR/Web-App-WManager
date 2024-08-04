package com.AloisioUmerto.Tesi.DataHandler.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SensorDto {
    private String CompanyName;
    private String SensorId;
    private double latitude;
    private double longitude;
    private String id;
}
