package com.myTesi.aloisioUmberto.dto;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class SensorDataInterestAreaDto {
    List<SensorData> sensorData;
    HashSet<String> sensorAreaTypes;
}
