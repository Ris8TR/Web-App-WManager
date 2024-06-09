package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class SensorDataDto {
    private String id;
    private String userId;
    private String dataType;
    private LocalDateTime date;
    private Date timestamp;
    private Object payload;
    private double latitude;
    private double longitude;

}
