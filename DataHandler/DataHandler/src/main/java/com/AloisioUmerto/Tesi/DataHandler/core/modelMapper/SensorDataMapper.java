package com.AloisioUmerto.Tesi.DataHandler.core.modelMapper;



import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import com.AloisioUmerto.Tesi.DataHandler.dto.NewSensorDataDto;
import com.AloisioUmerto.Tesi.DataHandler.dto.SensorDataDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper
public interface SensorDataMapper {
    SensorDataMapper INSTANCE = Mappers.getMapper(SensorDataMapper.class);

    @Mapping(target = "id", ignore = true)
    SensorData newSensorDataDtoToSensorData(NewSensorDataDto newSensorDataDto);

    @Mapping(target = "id", ignore = true)
    SensorDataDto sensorDataToSensorDataDto(SensorData sensorData);
}


