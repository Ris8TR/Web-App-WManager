package com.myTesi.aloisioUmberto.core.modelMapper;

import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SensorMapper {
    SensorMapper INSTANCE = Mappers.getMapper(SensorMapper.class);

    SensorDto sensorToSensorDto(Sensor sensor);

    @Mapping(target = "id", ignore = true)
    Sensor newSensorDtoToSensor(NewSensorDto newSensorDto);

    default String map(ObjectId value) {
        return value != null ? value.toString() : null;
    }

    default ObjectId map(String value) {
        return value != null ? new ObjectId(value) : null;
    }
}