package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class SensorDto {
    private String companyName;
    private String userId;
    private String id;
    private String password;
    private String token;
    private String description;
    private String interestAreaID;
    private List<Double> Latitude;
    private List<Double> Longitude;
}
