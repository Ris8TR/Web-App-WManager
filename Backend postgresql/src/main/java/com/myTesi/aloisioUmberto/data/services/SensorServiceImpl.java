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
import org.opengis.filter.identity.ObjectId;
import org.slf4j.Logger;
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
                ObjectMapper objectMapper = new ObjectMapper();
                Map data = objectMapper.readValue(file.getInputStream(), Map.class);

                String companyName = Objects.requireNonNull(data.get("companyName")).toString();
                String token = Objects.requireNonNull(data.get("token")).toString();
                String password = Objects.requireNonNull(data.get("password")).toString();
                String interestAreaId = Objects.requireNonNull(data.get("interestAreaId")).toString();
                String description = Objects.requireNonNull(data.get("description")).toString();
                String isPublic = Objects.requireNonNull(data.get("isPublic")).toString();
                String payloadType = Objects.requireNonNull(data.get("payloadType")).toString();

                // IMPORTANTE: usa isValidToken per ottenere l'userId
                String userId = isValidToken(token);
                if (userId == null) {
                    throw new IllegalArgumentException("Invalid token");
                }

                System.out.println("Creating sensor for user: " + userId);

                Optional<User> user = userDao.findById(userId);
                if (user.isPresent() && BCrypt.checkpw(password, user.get().getSensorPassword())) {
                    boolean exists = sensorRepository.existsByCompanyNameAndUserIdAndInterestAreaIDAndDescription(
                            companyName, userId, interestAreaId, description);

                    if (exists) {
                        throw new RuntimeException("A sensor with this data already exists");
                    }

                    Sensor newSensor = new Sensor();
                    newSensor.setCompanyName(companyName);
                    newSensor.setUserId(userId);  // ASSICURATI che questo sia impostato
                    newSensor.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
                    newSensor.setInterestAreaID(interestAreaId);
                    newSensor.setDescription(description);
                    newSensor.setIsPublic(Boolean.valueOf(isPublic));
                    newSensor.setPayloadType(PayloadType.valueOf(payloadType));
                    // Nota: manca setType() - potrebbe essere null

                    sensorRepository.save(newSensor);

                    System.out.println("Sensor saved with userId: " + newSensor.getUserId());

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
        System.out.println(newSensorDto);

        // 1. Estrai il token e verificalo
        String token = newSensorDto.getToken();

        // Verifica token e ottieni userId in un colpo solo
        String realUserId = isValidToken(token);
        if (realUserId == null) {
            throw new RuntimeException("Token non valido");
        }

        // Estrai email dal token (opzionale, per debug)
        String email = jwtTokenProvider.getEmailFromUserToken(token);
        System.out.println("Email: " + email);
        System.out.println("Real User ID: " + realUserId);

        Optional<User> user = userDao.findUserByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("Utente non trovato");
        }

        if (BCrypt.checkpw(newSensorDto.getPassword(), user.get().getSensorPassword())) {

            // Configurazione Sensore - ASSICURATI che userId sia impostato
            sensor.setPassword(BCrypt.hashpw(newSensorDto.getPassword(), BCrypt.gensalt(10)));
            sensor.setDescription(newSensorDto.getDescription());
            sensor.setPayloadType(newSensorDto.getPayloadType());
            sensor.setType(newSensorDto.getType());
            sensor.setUserId(realUserId);
            sensor.setCompanyName(newSensorDto.getCompanyName());
            sensor.setIsPublic(newSensorDto.getIsPublic());
            sensor.setInterestAreaID(newSensorDto.getInterestAreaId());

            System.out.println("Saving sensor with userId: " + realUserId);

            // Configurazione ColorBar
            ColorBar colorBar = new ColorBar();
            colorBar.setUserId(realUserId);
            colorBar.setName("ColorBar_" + realUserId);

            List<ColorRange> ranges = new ArrayList<>();
            ranges.add(new ColorRange(0.0, 10.0, "#FF0000", "Low"));
            ranges.add(new ColorRange(11.0, 20.0, "#00FF00", "Medium"));
            ranges.add(new ColorRange(21.0, 30.0, "#0000FF", "High"));
            colorBar.setColorRanges(ranges);

            // Salva ColorBar
            colorBarRepository.save(colorBar);

            try {
                sensor.setColorBarId(colorBar.getId());
                colorBar.addSensor(sensor.getId());

                sensorRepository.save(sensor);
                colorBarRepository.save(colorBar);

                // Mapping DTO di ritorno
                SensorDto sensorDto = sensorMapper.sensorToSensorDto(sensor);
                sensorDto.setUserId(realUserId);
                return sensorDto;

            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Errore integrità dati", e);
            }
        }
        throw new RuntimeException("Invalid credentials");
    }

    @Override
    public List<SensorDto> findPublicByCompanyName(String companyName) {

        List<Sensor> sensors = sensorRepository.findAllByCompanyNameAndIsPublic(companyName, true);

        if (sensors == null || sensors.isEmpty()) {
            return Collections.emptyList();
        }

        return sensors.stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SensorDto> findPublicById(String id) {

        Optional<Sensor> sensor = sensorRepository.findByUserIdAndIsPublic(id, true);
        if (sensor.isPresent()) {
            SensorDto sensorDto = sensorMapper.sensorToSensorDto(sensor.get());
            return Optional.ofNullable(sensorDto);
        }
        return Optional.empty();    }

    @Override
    public List<SensorDto> findPublicByType(String type) {
        List<Sensor> sensors = sensorRepository.findAllByTypeAndIsPublic(type, true);
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

        // Poiché abbiamo filtrato i null, il numero di DTO è uguale al numero di SensorData trovati
        int sensorDataCount = sensorDtoList.size();
        long duration = System.currentTimeMillis() - startTime;

        // 4. Stampa il Report dettagliato (Identico richiesto)
        System.out.println("------------------------------------------");
        System.out.println("REPORT ESECUZIONE (getAllSensor - Stream):");
        System.out.println("- Tempo impiegato: " + duration + " ms");
        System.out.println("- Record sensori pubblici analizzati: " + totalPublicSensors);
        System.out.println("- Record SensorData trovati: " + sensorDataCount);
        System.out.println("- Record SensorDto generati: " + sensorDtoList.size());
        System.out.println("");
        System.out.println("- [STRATEGIA]: Stream API (N+1 Query)");
        System.out.println("- [AVVISO]: Se il tempo è > 1000ms, considera l'ottimizzazione SQL.");
        System.out.println("------------------------------------------");

        System.out.println("getAllSensor: processati {" + totalPublicSensors + " sensori in " + duration +" ms");

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
        Optional<InterestArea> interestArea = interestAreaRepository.findById(interestAreaId);

        if (interestArea.isEmpty()) {
            throw new IllegalArgumentException("Interest area not found");
        }

        InterestArea area = interestArea.get();

        Boolean isPublic = area.getIsPublic();
        if (isPublic == null) {
            isPublic = false;
        }
        if (isPublic) {
            // L'area è pubblica, restituisci solo i sensori pubblici senza autenticazione
            List<Sensor> publicSensors = sensorRepository.findAllByInterestAreaIDAndIsPublicTrue(interestAreaId);
            if (publicSensors.isEmpty()) {
                return Collections.emptyList();
            }
            return publicSensors.stream()
                    .map(sensorMapper::sensorToSensorDto)
                    .collect(Collectors.toList());
        } else {
            // L'area non è pubblica, verifica il token e restituisci i sensori associati all'utente
            String userId = isValidToken(token);
            if (userId == null) {
                throw new IllegalArgumentException("Invalid user ID");
            }

            Optional<User> user = userDao.findById(userId);
            assert user.isPresent();

            List<Sensor> userSensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
            if (userSensors.isEmpty()) {
                return Collections.emptyList();
            }
            return userSensors.stream()
                    .map(sensorMapper::sensorToSensorDto)
                    .collect(Collectors.toList());
        }
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
        existingSensor.setIsPublic(sensorDto.getIsPublic());
        existingSensor.setType(String.valueOf(sensorDto.getType()));
        existingSensor.setDescription(sensorDto.getDescription());
        existingSensor.setCompanyName(sensorDto.getCompanyName());
        existingSensor.setInterestAreaID(sensorDto.getInterestAreaID());
        existingSensor.setColorBarId(sensorDto.getColorBarId());
        if (!Objects.equals(sensorDto.getPassword(), "")) {
            existingSensor.setPassword(BCrypt.hashpw(sensorDto.getPassword(), BCrypt.gensalt(10)));;
        }

        Sensor updatedSensor = sensorRepository.save(existingSensor);
        return modelMapper.map(updatedSensor, SensorDto.class);
    }

    @Override
    public void deleteSensorById(ObjectId id, String token) {
        final String userId = isValidToken(token);
        assert userId != null;
        Optional<Sensor> sensor = sensorRepository.findByIdAndUserId(String.valueOf(id), userId);
        assert sensor.isPresent();
        sensorRepository.deleteById(String.valueOf(sensor.get().getId()));

    }


}

