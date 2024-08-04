package com.myTesi.aloisioUmberto.core.modelMapper;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Mapper
public interface SensorDataMapper {
    SensorDataMapper INSTANCE = Mappers.getMapper(SensorDataMapper.class);

    SensorDataDto sensorDataToSensorDataDto(SensorData sensorData);

    @Mapping(target = "id", ignore = true)
    SensorData newSensorDataDtoToSensorData(NewSensorDataDto newSensorDataDto);

    default String map(ObjectId value) {
        return value != null ? value.toString() : null;
    }

    default ObjectId map(String value) {
        return value != null ? new ObjectId(value) : null;
    }

    default LocalDateTime map(Date value) {
        return value != null ? value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    default Date map(LocalDateTime value) {
        return value != null ? Date.from(value.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}

