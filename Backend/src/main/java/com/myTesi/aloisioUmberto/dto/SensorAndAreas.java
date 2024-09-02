package com.myTesi.aloisioUmberto.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class SensorAndAreas {
    List<SensorDto> sensorDtoList;
    List<InterestAreaDto> areaDtoList;
}
