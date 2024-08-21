package com.myTesi.aloisioUmberto.dto.New;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@ToString
public class NewSensorDto{

    private String companyName;
    private String password;
    private String SensorId;
    private String description;
    private String userId;
    private String interestAreaId;


}
