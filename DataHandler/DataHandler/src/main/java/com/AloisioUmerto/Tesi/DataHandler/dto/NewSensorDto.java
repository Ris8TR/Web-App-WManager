package com.AloisioUmerto.Tesi.DataHandler.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@ToString
public class NewSensorDto {

    private String CompanyName;
    private String password;
    private String SensorId;


}
