package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorMapper;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import com.myTesi.aloisioUmberto.dto.enumetation.Role;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    @Autowired
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SensorMapper sensorMapper = SensorMapper.INSTANCE;

    @Override
    public SensorDto saveDto(NewSensorDto newSensorDto) {
        Sensor sensor = sensorMapper.newSensorDtoToSensor(newSensorDto);
        sensor.setPassword(BCrypt.hashpw(sensor.getPassword(), BCrypt.gensalt(10)));
        try {
            sensorRepository.save(sensor);
            SensorDto sensorDto = sensorMapper.sensorToSensorDto(sensor);
            sensorDto.setId(sensor.getId().toString());
            return sensorDto;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "QUALCOSA NON E' ANDATO PER IL VERSO GIUSTO", e);
        }
    }

    @Override
    public Optional<SensorDto> findById(String id) {
        Optional<Sensor> sensor = sensorRepository.findById(id);
        if (sensor.isPresent()) {
            SensorDto sensorDto = sensorMapper.sensorToSensorDto(sensor.get());
            return Optional.ofNullable(sensorDto);
        }
        return Optional.empty();
    }

    @Override
    public List<SensorDto> findByCompanyName(String companyName) {
        List<Sensor> sensors = sensorRepository.findAllByCompanyName(companyName);

        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorDto> getAllSensor() {
        List<Sensor> sensors = sensorRepository.findAll();
        List<SensorDto> sensorDtoList = new ArrayList<>();
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        // Log per debugging
        System.out.println("Current Time: " + now);
        System.out.println("Ten Minutes Ago: " + tenMinutesAgo);

        for (Sensor sensor : sensors) {
            Optional<SensorData> sensorDataOptional = sensorDataRepository.findTopByTimestampBetweenAndSensorId(tenMinutesAgo, now, sensor.getId().toString());

            // Log per debugging
            System.out.println("Sensor ID: " + sensor.getId());
            if (sensorDataOptional.isPresent()) {
                System.out.println("SensorData found for Sensor ID: " + sensor.getId());
                SensorData sensorData = sensorDataOptional.get();
                SensorDto sensorDto = new SensorDto();
                sensorDto.setCompanyName(sensor.getCompanyName());
                sensorDto.setLatitude(sensorData.getLatitude());
                sensorDto.setLongitude(sensorData.getLongitude());
                sensorDtoList.add(sensorDto);
            } else {
                System.out.println("No SensorData found for Sensor ID: " + sensor.getId());
            }
        }

        System.out.println(sensorDtoList);
        return sensorDtoList;
    }
}
