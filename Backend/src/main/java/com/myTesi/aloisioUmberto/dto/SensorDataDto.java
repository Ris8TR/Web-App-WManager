package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SensorDataDto {
    private String id;
    private String userId;
    private String dataType;
    private String data;
    private long timestamp;
    private double latitude;
    private double longitude;

}
