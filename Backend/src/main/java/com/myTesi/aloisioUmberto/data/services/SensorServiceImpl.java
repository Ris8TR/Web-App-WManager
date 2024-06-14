package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
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
    private final ModelMapper modelMapper = new ModelMapper();
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public NewSensorDto saveDto(NewSensorDto newSensorDto) {
        Sensor sensor = modelMapper.map(newSensorDto, Sensor.class);
        sensor.setPassword(BCrypt.hashpw(sensor.getPassword(), BCrypt.gensalt(10)));
        try {
            sensorRepository.save(sensor);
            return modelMapper.map(sensor, NewSensorDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "QUALCOSA NON E' ANDATO PER IL VERSO GIUSTO", e);
        }

    }

    @Override
    public Optional<SensorDto> findById(String id) {
        Optional<Sensor> sensor = sensorRepository.findById(id);
        if (sensor.isPresent()) {
            SensorDto sensorDto = modelMapper.map(sensor.get(), SensorDto.class);
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

        List<SensorDto> sensorDtos = sensors.stream()
                .map(sensor -> modelMapper.map(sensor, SensorDto.class))
                .collect(Collectors.toList());
        return sensorDtos;
    }



    //TODO QUESTA PARTE ANDRA' OTTIMIZZATA NON SI POSSONO PRENDERE TUTTI I DATI OGNI VOLTA PER TROVARE L'ULTIMA POSIZIONE NOTA
    //SAREBBE MEGLIO O CREARE UN ENTITA' A PARTE PER I SENSORI E/O CREARE 2 CAMPI (DENTRO USER) DOVE OGNI CARICAMENTO DI DATO
    //VIENE AGGIORNATA LAT E LON

    @Override
    public List<SensorDto> getAllSensor() {
        List<Sensor> sensors = sensorRepository.findAll();
        List<SensorDto> sensorDtoList = new ArrayList<>();
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();
        for (Sensor sensor : sensors) {
            Optional<SensorData> sensorDataOptional = sensorDataRepository.findTopByTimestampBetweenAndSensorId(tenMinutesAgo, now,sensor.getId().toString());
            if (sensorDataOptional.isPresent()) {
                SensorData sensorData = sensorDataOptional.get();
                SensorDto sensorDto = new SensorDto();
                sensorDto.setCompanyName(sensor.getCompanyName());
                sensorDto.setLatitude(sensorData.getLatitude());
                sensorDto.setLongitude(sensorData.getLongitude());
                sensorDtoList.add(sensorDto);
            }
        }

        return sensorDtoList;
    }
}
