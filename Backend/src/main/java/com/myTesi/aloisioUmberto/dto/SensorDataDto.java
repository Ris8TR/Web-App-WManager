package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class SensorDataDto {
    private String id;
    private String userId;
    private String dataType;
    private String data;
    private Date timestamp;
    private double latitude;
    private double longitude;

}
