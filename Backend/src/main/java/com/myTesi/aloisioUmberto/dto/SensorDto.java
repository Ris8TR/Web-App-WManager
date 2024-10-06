package com.myTesi.aloisioUmberto.dto;

import com.myTesi.aloisioUmberto.dto.enumetation.PayloadType;
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
    private String colorBarId;
    private PayloadType payloadType;
    private String description;
    private String type;
    private String interestAreaID;
    private List<Double> Latitude;
    private List<Double> Longitude;
}
