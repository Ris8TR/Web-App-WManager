package com.myTesi.aloisioUmberto.core.modelMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.opengis.filter.identity.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Mapper
public interface SensorDataMapper {
    SensorDataMapper INSTANCE = Mappers.getMapper(SensorDataMapper.class);

    SensorDataDto sensorDataToSensorDataDto(SensorData sensorData);

    @Mapping(target = "payload", source = "payload")
    SensorData newSensorDataDtoToSensorData(NewSensorDataDto newSensorDataDto);

    // MapStruct userà automaticamente questo metodo quando trova Object -> String
    default String map(Object value) {
        if (value == null) return null;
        try {
            // Converte l'oggetto (Map, List, ecc.) in una stringa JSON valida per Postgres
            return new ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }


    default LocalDateTime map(Date value) {
        return value != null ? value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    default Date map(LocalDateTime value) {
        return value != null ? Date.from(value.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
}

