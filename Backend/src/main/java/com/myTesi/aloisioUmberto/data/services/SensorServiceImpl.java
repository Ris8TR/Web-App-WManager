package com.myTesi.aloisioUmberto.data.services;

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
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorService;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.SensorAndAreas;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import com.myTesi.aloisioUmberto.dto.enumetation.PayloadType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final ColorBarRepository colorBarRepository;
    private final SensorDataRepository sensorDataRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userDao;
    private final InterestAreaRepository interestAreaRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    private final SensorMapper sensorMapper = SensorMapper.INSTANCE;
    private final InterestAreaMapper interestAreaMapper = InterestAreaMapper.INSTANCE;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Helper per validare il token e restituire l'ID utente.
     */
    private String getValidatedUserId(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromUserToken(token);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
    }

    /**
     * Helper per recuperare l'utente e validare le credenziali.
     */
    private User validateUserCredentials(String userId, String rawPassword) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!BCrypt.checkpw(rawPassword, user.getSensorPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid sensor credentials");
        }
        return user;
    }

    @Override
    @Transactional
    public SensorDto save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is empty");
        }

        Map<String, Object> data = objectMapper.readValue(file.getInputStream(), Map.class);

        String token = String.valueOf(data.get("token"));
        String userId = getValidatedUserId(token);
        String password = String.valueOf(data.get("password"));

        validateUserCredentials(userId, password);

        String companyName = String.valueOf(data.get("companyName"));
        String interestAreaId = String.valueOf(data.get("interestAreaId"));
        String description = String.valueOf(data.get("description"));

        if (sensorRepository.existsByCompanyNameAndUserIdAndInterestAreaIDAndDescription(
                companyName, userId, interestAreaId, description)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A sensor with this data already exists");
        }

        Sensor newSensor = new Sensor();
        newSensor.setCompanyName(companyName);
        newSensor.setUserId(userId);
        newSensor.setPassword(passwordEncoder.encode(password));
        newSensor.setInterestAreaID(interestAreaId);
        newSensor.setDescription(description);
        newSensor.setIsPublic(Boolean.valueOf(String.valueOf(data.get("isPublic"))));
        newSensor.setPayloadType(PayloadType.valueOf(String.valueOf(data.get("payloadType"))));

        Sensor saved = sensorRepository.save(newSensor);
        SensorDto dto = modelMapper.map(saved, SensorDto.class);
        dto.setId(saved.getId().toString());
        return dto;
    }

    @Override
    @Transactional
    public SensorDto saveDto(NewSensorDto newSensorDto) {
        String email = jwtTokenProvider.getEmailFromUserToken(newSensorDto.getToken());
        User user = userDao.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!BCrypt.checkpw(newSensorDto.getPassword(), user.getSensorPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // Creazione ColorBar
        ColorBar colorBar = new ColorBar();
        colorBar.setUserId(String.valueOf(user.getId()));
        colorBar.setName("ColorBar_" + user.getId());

        // Setup Range Predefiniti
        colorBar.setColorRanges(Arrays.asList(
                new ColorRange(0, 10, "#FF0000"),
                new ColorRange(11, 20, "#00FF00"),
                new ColorRange(21, 30, "#0000FF")
        ));

        ColorBar savedBar = colorBarRepository.save(colorBar);

        // Creazione Sensore
        Sensor sensor = new Sensor();
        sensor.setPassword(passwordEncoder.encode(newSensorDto.getPassword()));
        sensor.setDescription(newSensorDto.getDescription());
        sensor.setPayloadType(newSensorDto.getPayloadType());
        sensor.setType(newSensorDto.getType());
        sensor.setUserId(String.valueOf(user.getId()));
        sensor.setCompanyName(newSensorDto.getCompanyName());
        sensor.setIsPublic(newSensorDto.getIsPublic());
        sensor.setInterestAreaID(newSensorDto.getInterestAreaId());
        sensor.setColorBarId(String.valueOf(savedBar.getId()));

        Sensor savedSensor = sensorRepository.save(sensor);

        // Aggiornamento riferimento circolare ColorBar -> Sensor
        savedBar.addSensor(String.valueOf(savedSensor.getId()));
        colorBarRepository.save(savedBar);

        SensorDto sensorDto = sensorMapper.sensorToSensorDto(savedSensor);
        sensorDto.setId(savedSensor.getId().toString());
        sensorDto.setUserId(String.valueOf(user.getId()));
        return sensorDto;
    }

    @Override
    public List<SensorDto> findPublicByCompanyName(String companyName) {
        return sensorRepository.findAllByCompanyNameAndIsPublic(companyName, true)
                .stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SensorDto> findPublicById(String id) {
        return sensorRepository.findByUserIdAndIsPublic(id, true)
                .map(sensorMapper::sensorToSensorDto);
    }

    @Override
    public List<SensorDto> findPublicByType(String type) {
        return sensorRepository.findAllByTypeAndIsPublic(type, true)
                .stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SensorDto> findById(String id, String token) {
        String userId = getValidatedUserId(token);
        return sensorRepository.findById(id)
                .filter(s -> s.getUserId().equals(userId))
                .map(sensorMapper::sensorToSensorDto);
    }

    @Override
    public List<SensorDto> findByCompanyName(String companyName, String token) {
        String userId = getValidatedUserId(token);
        return sensorRepository.findAllByCompanyNameAndUserId(companyName, userId)
                .stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }



    @Override
    public List<SensorDto> getAllSensor() {
        long startTime = System.currentTimeMillis();

        List<Sensor> sensors = sensorRepository.findAllByIsPublic(true);
        int totalPublicSensors = sensors.size();

        List<SensorDto> sensorDtoList = sensors.stream()
                .map(sensor -> sensorDataRepository.findTopBySensorIdOrderByTimestampDesc(String.valueOf(sensor.getId()))
                        .map(data -> {
                            SensorDto dto = new SensorDto();
                            dto.setId(String.valueOf(sensor.getId()));
                            dto.setDescription(sensor.getDescription());
                            dto.setUserId(String.valueOf(sensor.getUserId()));
                            dto.setPayloadType(sensor.getPayloadType());
                            dto.setInterestAreaID(sensor.getInterestAreaID());
                            dto.setCompanyName(sensor.getCompanyName());
                            dto.setLatitude(Collections.singletonList(data.getLatitude()));
                            dto.setLongitude(Collections.singletonList(data.getLongitude()));
                            dto.setTimestamp(String.valueOf(data.getTimestamp()));
                            dto.setIsPublic(sensor.getIsPublic());
                            return dto;
                        }).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int sensorDataCount = sensorDtoList.size();
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("------------------------------------------");
        System.out.println("REPORT ESECUZIONE (getAllSensor):");
        System.out.println("- Tempo impiegato: " + duration + " ms");
        System.out.println("- Record sensori pubblici analizzati: " + totalPublicSensors);
        System.out.println("- Record SensorData trovati: " + sensorDataCount);
        System.out.println("- Record SensorDto generati: " + sensorDtoList.size());
        System.out.println("");
        System.out.println("------------------------------------------");

        log.info("getAllSensor: processati {} sensori in {} ms", totalPublicSensors, duration);

        return sensorDtoList;
    }

    @Override
    public List<SensorDto> findByUserId(String token) {
        String userId = getValidatedUserId(token);
        return sensorRepository.findAllByUserId(userId)
                .stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public SensorAndAreas findAndAreaByUserId(String token) {
        String userId = getValidatedUserId(token);

        if (!userDao.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        List<Sensor> sensors = sensorRepository.findAllByUserId(userId);
        List<InterestAreaDto> areas = interestAreaRepository.findAllByUserId(userId)
                .stream()
                .map(interestAreaMapper::interestAreaToInterestAreaDto)
                .collect(Collectors.toList());

        SensorAndAreas result = new SensorAndAreas();
        result.setSensorDtoList(sensorMapper.sensorsToSensorDtos(sensors));
        result.setAreaDtoList(areas);
        return result;
    }

    @Override
    public List<SensorDto> findByTypeAndUser(String type, String token) {
        String userId = getValidatedUserId(token);
        return sensorRepository.findAllByUserIdAndType(userId, type)
                .stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorDto> findByInterestAreaId(String interestAreaId, String token) {
        InterestArea area = interestAreaRepository.findById(interestAreaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interest area not found"));

        if (Boolean.TRUE.equals(area.getIsPublic())) {
            return sensorRepository.findAllByInterestAreaIDAndIsPublicTrue(interestAreaId)
                    .stream()
                    .map(sensorMapper::sensorToSensorDto)
                    .collect(Collectors.toList());
        }

        String userId = getValidatedUserId(token);
        return sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId)
                .stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SensorDto update(SensorDto sensorDto) {
        String userId = getValidatedUserId(sensorDto.getToken());

        Sensor existingSensor = sensorRepository.findById(sensorDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found"));

        if (!existingSensor.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this sensor");
        }

        existingSensor.setIsPublic(sensorDto.getIsPublic());
        existingSensor.setType(String.valueOf(sensorDto.getType()));
        existingSensor.setDescription(sensorDto.getDescription());
        existingSensor.setCompanyName(sensorDto.getCompanyName());
        existingSensor.setInterestAreaID(sensorDto.getInterestAreaID());
        existingSensor.setColorBarId(sensorDto.getColorBarId());

        if (sensorDto.getPassword() != null && !sensorDto.getPassword().isBlank()) {
            existingSensor.setPassword(passwordEncoder.encode(sensorDto.getPassword()));
        }

        Sensor updated = sensorRepository.save(existingSensor);
        return modelMapper.map(updated, SensorDto.class);
    }

    @Override
    @Transactional
    public void deleteSensorById(ObjectId id, String token) {
        String userId = getValidatedUserId(token);
        Sensor sensor = sensorRepository.findByIdAndUserId(id.toString(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found or access denied"));

        sensorRepository.deleteById(sensor.getId().toString());
    }
}