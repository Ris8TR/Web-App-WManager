package com.myTesi.aloisioUmberto.core.modelMapper;

import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface SensorMapper {
    SensorMapper INSTANCE = Mappers.getMapper(SensorMapper.class);

    SensorDto sensorToSensorDto(Sensor sensor);

    Sensor newSensorDtoToSensor(NewSensorDto newSensorDto);

    default String map(ObjectId value) {
        return value != null ? value.toString() : null;
    }

    default ObjectId map(String value) {
        return value != null ? new ObjectId(value) : null;
    }

    default List<SensorDto> sensorsToSensorDtos(List<Sensor> sensors) {
        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }
        return sensors.stream()
                .map(this::sensorToSensorDto)
                .collect(Collectors.toList());
    }
}