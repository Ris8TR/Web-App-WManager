package com.myTesi.aloisioUmberto.data.services;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorMapper;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    @Autowired
    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userDao;
    private final SensorMapper sensorMapper = SensorMapper.INSTANCE;
    @Autowired
    private ModelMapper modelMapper;



    private String isValidToken(String token) {
        if (jwtTokenProvider.validateToken(token))
            return jwtTokenProvider.getUserIdFromUserToken(token);
        return null;
    }

    @Override
    public SensorDto saveDto(NewSensorDto newSensorDto) {
        Sensor sensor = sensorMapper.newSensorDtoToSensor(newSensorDto);
        String userId = isValidToken(newSensorDto.getUserId());
        assert userId != null;
        Optional<User> user = userDao.findById(userId);


        if (BCrypt.checkpw(newSensorDto.getPassword(), user.get().getSensorPassword())) {
            sensor.setPassword(BCrypt.hashpw(sensor.getPassword(), BCrypt.gensalt(10)));
            sensor.setDescription(newSensorDto.getDescription());
            sensor.setUserId(String.valueOf(user.get().getId()));
            sensor.setInterestAreaID(newSensorDto.getInterestAreaId());
            try {
                sensorRepository.save(sensor);
                SensorDto sensorDto = sensorMapper.sensorToSensorDto(sensor);
                sensorDto.setId(sensor.getId().toString());
                sensorDto.setDescription(sensor.getDescription());
                sensorDto.setUserId(String.valueOf(user.get().getId()));
                sensorDto.setInterestAreaID(sensor.getInterestAreaID());
                return sensorDto;
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QUALCOSA NON E' ANDATO PER IL VERSO GIUSTO", e);
            }
        }
        throw new RuntimeException("Invalid credentials");

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
                sensorDto.setLatitude(Collections.singletonList(sensorData.getLatitude()));
                sensorDto.setLongitude(Collections.singletonList(sensorData.getLongitude()));
                sensorDtoList.add(sensorDto);
            } else {
                System.out.println("No SensorData found for Sensor ID: " + sensor.getId());
            }
        }

        System.out.println(sensorDtoList);
        return sensorDtoList;
    }

    @Override
    public SensorDto save(MultipartFile file) throws IOException {

        if (file != null && !file.isEmpty()) {
            try {
                // Parse the JSON file
                ObjectMapper objectMapper = new ObjectMapper();
                Map data = objectMapper.readValue(file.getInputStream(), Map.class);

                String companyName = data.get("companyName").toString();
                String userId = data.get("userId").toString();
                String password = data.get("password").toString();
                String interestAreaId = data.get("interestAreaId").toString();
                String description = data.get("description").toString();

                Optional<User> user = userDao.findById(userId);
                if (user.isPresent() && BCrypt.checkpw(password, user.get().getSensorPassword())) {

                    boolean exists = sensorRepository.existsByCompanyNameAndUserIdAndInterestAreaIDAndDescription(
                            companyName, userId, interestAreaId, description);

                    if (exists) {
                        throw new RuntimeException("A sensor with this data already exists");
                    }

                    Sensor newSensor = new Sensor();
                    newSensor.setCompanyName(companyName);
                    newSensor.setUserId(userId);
                    newSensor.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
                    newSensor.setInterestAreaID(interestAreaId);
                    newSensor.setDescription(description);

                    sensorRepository.save(newSensor);

                    SensorDto sensorDto = modelMapper.map(newSensor, SensorDto.class);
                    sensorDto.setId(newSensor.getId().toString());
                    return sensorDto;

                } else {
                    throw new RuntimeException("Invalid user credentials");
                }

            } catch (Exception e) {
                throw new RuntimeException("Error during JSON file parsing", e);
            }
        } else {
            throw new RuntimeException("The uploaded file is empty");
        }
    }
}

