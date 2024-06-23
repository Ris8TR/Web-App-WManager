package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorDto;

import java.util.List;
import java.util.Optional;

public interface SensorService {

    Optional<SensorDto> findById(String id);
    List<SensorDto> findByCompanyName(String companyName);
    SensorDto saveDto(NewSensorDto newUserDto);
    List<SensorDto> getAllSensor();


}
