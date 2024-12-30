package org.data.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class NewSensorDto {

    private String companyName;
    private String password;
    private Boolean isPublic;
    private String SensorId;
    private String description;
    private String userId;
    private String interestAreaId;

}
