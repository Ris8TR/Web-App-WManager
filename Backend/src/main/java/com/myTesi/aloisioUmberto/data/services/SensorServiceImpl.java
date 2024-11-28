package com.myTesi.aloisioUmberto.data.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.InterestAreaMapper;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorMapper;
import com.myTesi.aloisioUmberto.data.dao.*;
import com.myTesi.aloisioUmberto.data.entities.Bar.ColorBar;
import com.myTesi.aloisioUmberto.data.entities.Bar.ColorRange;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorService;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorAndAreas;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import com.myTesi.aloisioUmberto.dto.enumetation.PayloadType;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    @Autowired
    private final SensorRepository sensorRepository;
    private final ColorBarRepository colorBarRepository;
    private final SensorDataRepository sensorDataRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userDao;
    private final SensorMapper sensorMapper = SensorMapper.INSTANCE;
    private final InterestAreaMapper interestAreaMapper = InterestAreaMapper.INSTANCE;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private InterestAreaRepository interestAreaRepository;


    private String isValidToken(String token) {
        if (jwtTokenProvider.validateToken(token))
            return jwtTokenProvider.getUserIdFromUserToken(token);
        return null;

    }


    @Override
    public SensorDto save(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            try {
                // Parse del file JSON
                ObjectMapper objectMapper = new ObjectMapper();
                Map data = objectMapper.readValue(file.getInputStream(), Map.class);

                String companyName = Objects.requireNonNull(data.get("companyName")).toString();
                String token = Objects.requireNonNull(data.get("token")).toString();
                String password = Objects.requireNonNull(data.get("password")).toString();
                String interestAreaId = Objects.requireNonNull(data.get("interestAreaId")).toString();
                String description = Objects.requireNonNull(data.get("description")).toString();
                String userId = isValidToken(token);
                if (userId == null) {
                    throw new IllegalArgumentException("Invalid user ID");
                }
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
                    newSensor.setInterestAreaID(interestAreaId);

                    sensorRepository.save(newSensor);

                    SensorDto sensorDto = modelMapper.map(newSensor, SensorDto.class);
                    sensorDto.setId(newSensor.getId().toString());
                    return sensorDto;

                } else {
                    throw new RuntimeException("Invalid user credentials");
                }

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error during JSON file parsing", e);
            } catch (IOException e) {
                throw new RuntimeException("Error during file reading", e);
            }
        } else {
            throw new RuntimeException("The uploaded file is empty");
        }
    }



    @Override
    public SensorDto saveDto(NewSensorDto newSensorDto) {
        Sensor sensor = new Sensor();
        String email = jwtTokenProvider.getEmailFromUserToken(newSensorDto.getToken());
        Optional<User> user = userDao.findUserByEmail(email);
        assert user.isPresent();
        System.out.println(newSensorDto);
        System.out.println(user);
        if (BCrypt.checkpw(newSensorDto.getPassword(), user.get().getSensorPassword())) {
            sensor.setPassword(BCrypt.hashpw(newSensorDto.getPassword(), BCrypt.gensalt(10)));
            sensor.setDescription(newSensorDto.getDescription());
            sensor.setPayloadType(newSensorDto.getPayloadType());
            sensor.setType(newSensorDto.getType());
            sensor.setUserId(String.valueOf(user.get().getId()));
            sensor.setCompanyName(newSensorDto.getCompanyName());
            sensor.setInterestAreaID(newSensorDto.getInterestAreaId());
            ColorBar colorBar = new ColorBar();
            colorBar.setUserId(newSensorDto.getUserId());
            colorBar.setName("ColorBar"+newSensorDto.getUserId());

            //TODO TEST
            List<ColorRange> ranges = new ArrayList<>();
            ranges.add(new ColorRange(0, 10, "#FF0000"));
            ranges.add(new ColorRange(11, 20, "#00FF00"));
            ranges.add(new ColorRange(21, 30, "#0000FF"));

            colorBar.setColorRanges(ranges);
            //colorBar.setColorRanges(newSensorDto.getRanges());
            colorBarRepository.save(colorBar);
            try {
                sensor.setColorBarId(String.valueOf(colorBar.getId()));
                colorBar.addSensor(String.valueOf(sensor.getId()));
                sensorRepository.save(sensor);  // Salva una sola volta dopo aver completato le modifiche necessarie
                colorBarRepository.save(colorBar);

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
    public List<SensorDto> findPublicByCompanyName(String companyName) {

        List<Sensor> sensors = sensorRepository.findAllByCompanyNameAndVisibility(companyName, true);

        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SensorDto> findPublicById(String id) {

        Optional<Sensor> sensor = sensorRepository.findByUserIdAndVisibility(id, true);
        if (sensor.isPresent()) {
            SensorDto sensorDto = sensorMapper.sensorToSensorDto(sensor.get());
            return Optional.ofNullable(sensorDto);
        }
        return Optional.empty();    }

    @Override
    public List<SensorDto> findPublicByType(String type) {
        List<Sensor> sensors = sensorRepository.findAllByTypeAndVisibility(type, true);
        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }



    @Override
    public Optional<SensorDto> findById(String id, String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();
        Optional<Sensor> sensor = sensorRepository.findById(id);
        if (sensor.isPresent() && sensor.get().getUserId().equals(userId)) {
            SensorDto sensorDto = sensorMapper.sensorToSensorDto(sensor.get());
            return Optional.ofNullable(sensorDto);
        }
        return Optional.empty();
    }



    @Override
    public List<SensorDto> findByCompanyName(String companyName, String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();
        List<Sensor> sensors = sensorRepository.findAllByCompanyNameAndUserId(companyName, userId);

        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }



    @Override
    public List<SensorDto> getAllSensor() {
        List<Sensor> sensors = sensorRepository.findAllByVisibility(true);
        List<SensorDto> sensorDtoList = new ArrayList<>();


        for (Sensor sensor : sensors) {
            Optional<SensorData> sensorDataOptional = sensorDataRepository.findTopBySensorIdOrderByTimestampDesc(String.valueOf(sensor.getId()));


            if (sensorDataOptional.isPresent()) {
                SensorData sensorData = sensorDataOptional.get();
                SensorDto sensorDto = new SensorDto();
                sensorDto.setId(String.valueOf(sensor.getId()));
                sensorDto.setCompanyName(sensor.getCompanyName());
                sensorDto.setLatitude(Collections.singletonList(sensorData.getLatitude()));
                sensorDto.setLongitude(Collections.singletonList(sensorData.getLongitude()));
                sensorDto.setTimestamp(String.valueOf(sensorData.getTimestamp()));
                sensorDtoList.add(sensorDto);
            } else {
               System.out.println("No SensorData found for Sensor ID: " + sensor.getId());
            }
        }

        return sensorDtoList;
    }



    @Override
    public List<SensorDto> findByUserId(String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        System.out.println(userId);
        List<Sensor> sensors = sensorRepository.findAllByUserId(userId);
        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }




    @Override
    public SensorAndAreas findAndAreaByUserId(String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        List<Sensor> sensors = sensorRepository.findAllByUserId(userId);
        if (sensors.isEmpty()) {
            throw new IllegalArgumentException("No sensors found for the user");
        }

        List<SensorDto> sensorDtos = sensorMapper.sensorsToSensorDtos(sensors);
        List<InterestAreaDto> interestAreaDtos = interestAreaRepository.findAllByUserId(userId)
                .stream()
                .map(interestAreaMapper::interestAreaToInterestAreaDto)
                .collect(Collectors.toList());

        SensorAndAreas sensorAndAreas = new SensorAndAreas();
        sensorAndAreas.setSensorDtoList(sensorDtos);
        sensorAndAreas.setAreaDtoList(interestAreaDtos);
        return sensorAndAreas;
    }


    @Override
    public List<SensorDto> findByTypeAndUser(String type, String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        System.out.println(userId);
        List<Sensor> sensors = sensorRepository.findAllByUserIdAndType(userId, type);
        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorDto> findByInterestAreaId(String interestAreaId, String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();
        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);

        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public SensorDto update(SensorDto sensorDto) {
        String userId = isValidToken(sensorDto.getToken());
        if (userId == null) {
            throw new RuntimeException("Invalid token");
        }

        Sensor existingSensor = sensorRepository.findById(sensorDto.getId())
                .orElseThrow(() -> new RuntimeException("Sensor not found"));

        if (!Objects.equals(sensorDto.getUserId(), existingSensor.getUserId())) {
            throw new RuntimeException("Invalid credentials");
        }
        existingSensor.setVisibility(sensorDto.getVisibility());
        existingSensor.setType(String.valueOf(sensorDto.getType()));
        existingSensor.setDescription(sensorDto.getDescription());
        existingSensor.setCompanyName(sensorDto.getCompanyName());
        existingSensor.setInterestAreaID(sensorDto.getInterestAreaID());
        existingSensor.setColorBarId(sensorDto.getColorBarId());

        Sensor updatedSensor = sensorRepository.save(existingSensor);
        return modelMapper.map(updatedSensor, SensorDto.class);
    }


}

