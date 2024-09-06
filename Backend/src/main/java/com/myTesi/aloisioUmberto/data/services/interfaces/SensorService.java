package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorAndAreas;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SensorService {

    Optional<SensorDto> findById(String id, String token);
    List<SensorDto> findByCompanyName(String companyName, String token);
    SensorDto saveDto(NewSensorDto newSensorDto);
    List<SensorDto> getAllSensor();
    SensorDto save(MultipartFile file) throws IOException;

    List<SensorDto> findByUserId(String token);

    SensorAndAreas findAndAreaByUserId(String token);

    List<SensorDto> findByTypeAndUser(String type, String token);

    List<SensorDto> findByInterestAreaId(String interestAreaId, String token);

    SensorDto update(SensorDto sensorDto);

}
