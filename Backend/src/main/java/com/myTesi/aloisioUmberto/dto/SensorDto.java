package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class SensorDto {
    private String firstName;
    private double latitude;
    private double longitude;
}
