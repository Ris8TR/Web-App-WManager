package com.myTesi.aloisioUmberto.data.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myTesi.aloisioUmberto.JwtAuthConverter;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorDataMapper;
import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.*;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.DateDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataInterestAreaDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;
    @Autowired
    private SensorRepository sensorRepository;
    private final UserRepository userDao;
    private final SensorDataMapper sensorDataMapper = SensorDataMapper.INSTANCE;
    @Autowired
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    private final JwtAuthConverter jwtAuthConverter;


    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private InterestAreaRepository interestAreaRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public void SensorDataService(RedisTemplate<String, Object> redisTemplate,
                                  SensorDataRepository sensorDataRepository) {
        this.redisTemplate = redisTemplate;
        this.sensorDataRepository = sensorDataRepository;
    }

    @Transactional
    public SensorData saveSensorData(NewSensorDataDto newSensorDataDto) {
        // 1. Mappatura iniziale
        SensorData sensorData = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDto);
        System.out.println("++++++++++++++++++++++++++++" + sensorData);

        // 2. Estrai l'userId REALE dal token (se il token è valido)
        String realUserId = isValidToken(newSensorDataDto.getToken());
        if (realUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        // NON chiamare getUserIdFromUserToken() perché realUserId è già l'UUID!

        // 3. Recupero User e Sensor
        User user = userDao.findById(realUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Sensor sensor = sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), realUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found"));

        // 4. Controllo Password del Sensore
        if (BCrypt.checkpw(newSensorDataDto.getSensorPassword(), user.getSensorPassword())) {
            try {
                sensorData.setSensorId(sensor.getId());
                sensor.setInterestAreaID(sensorData.getInterestAreaID());

                // 5. Aggiornamento Coordinate
                if (sensor.getLongitude() == null) sensor.setLongitude(new ArrayList<>());
                if (sensor.getLatitude() == null) sensor.setLatitude(new ArrayList<>());

                if (!sensor.getLongitude().isEmpty() && !sensor.getLatitude().isEmpty()) {
                    Double lastLong = sensor.getLongitude().getFirst();
                    Double lastLat = sensor.getLatitude().getFirst();

                    if (!lastLong.equals(sensorData.getLongitude()) || !lastLat.equals(sensorData.getLatitude())) {
                        sensor.getLongitude().addFirst(sensorData.getLongitude());
                        sensor.getLatitude().addFirst(sensorData.getLatitude());
                    }
                } else {
                    sensor.getLongitude().add(sensorData.getLongitude());
                    sensor.getLatitude().add(sensorData.getLatitude());
                }

                sensorRepository.save(sensor);
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Database integrity error", e);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid sensor credentials");
        }

        // 6. Finalizzazione SensorData
        sensorData.setTimestamp(newSensorDataDto.getTimestamp());
        sensorData.setSavedOnTime(Date.from(Instant.now()));

        System.out.println("++++++++++++++++++++++++++++" + sensorData);

        return sensorDataRepository.save(sensorData);
    }


    @Override
    public SensorDataDto getLatestSensorDataBySensorId(String token, String id) {
        String userId = isValidToken(token);
        assert userId != null;
        Sensor sensor = sensorRepository.findByIdAndUserId(id, userId).orElse(null);
        assert sensor != null;
        Optional<SensorData> sensorData = sensorDataRepository.findTopBySensorId(sensor.getId().toString());
        return sensorData.map(data -> modelMapper.map(data, SensorDataDto.class)).orElse(null);
    }



    @Override
    public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
        SensorData data = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDTO);

        // Estrai userId dal token (senza chiamate duplicate)
        String token = newSensorDataDTO.getToken();
        String realUserId = isValidToken(token);
        if (realUserId == null) {
            throw new RuntimeException("Invalid Token");
        }

        System.out.println("Real User ID: " + realUserId);
        System.out.println("Sensor ID from DTO: " + newSensorDataDTO.getSensorId());

        // Opzione A: Cerca il sensore SENZA vincolo sull'userId
        Sensor sensor = sensorRepository.findById(newSensorDataDTO.getSensorId())
                .orElseThrow(() -> new RuntimeException("Sensor not found with id: " + newSensorDataDTO.getSensorId()));

        System.out.println("Found sensor: " + sensor.getId() + ", belongs to user: " + sensor.getUserId());

        // Opzione B: Verifica se l'utente ha accesso a questo sensore
        // (potresti volere una tabella di associazione sensori-utenti)

        User user = userDao.findById(realUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (BCrypt.checkpw(newSensorDataDTO.getSensorPassword(), user.getSensorPassword())) {
            data.setSensorId(sensor.getId());
            data.setInterestAreaID(sensor.getInterestAreaID());
        } else {
            throw new RuntimeException("Invalid credentials");
        }

        data.setTimestamp(newSensorDataDTO.getTimestamp());
        data.setSavedOnTime(Date.from(Instant.now()));

        // Gestione File e Payload
        if (file != null && !file.isEmpty()) {
            SensorDataHandler handler = getHandlerForType(sensor.getType());
            if (handler != null) {
                handler.handle(data, newSensorDataDTO, file);
            }
        } else if (newSensorDataDTO.getPayload() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(newSensorDataDTO.getPayload());
            data.setPayload(jsonPayload);
        }

        return sensorDataRepository.save(data);
    }

    @Override
    public SensorDataInterestAreaDto getTopSensorDataBySensorId(String sensorId, String token) {
        String userId = isValidToken(token);
        if (userId == null) return null;

        return sensorRepository.findByIdAndUserId(sensorId, userId)
                .map(sensor -> createSensorDataInterestAreaDto(String.valueOf(sensor.getId()), null, null))
                .orElse(null);
    }


    @Override
    public SensorDataInterestAreaDto getTopSensorDataByInterestAreaIdAndSensorId(String interestAreaId, String sensorId, String token) {
        String userId = isValidToken(token);
        if (userId == null) return null;

        Sensor sensor = sensorRepository.findByIdAndInterestAreaIDAndUserId(sensorId, interestAreaId, userId);
        return sensor != null ? createSensorDataInterestAreaDto(String.valueOf(sensor.getId()), null, null) : null;
    }


    @Override
    public SensorDataInterestAreaDto getTopSensorDataByInterestAreaId(String interestAreaId, String token) {
        String userId = isValidToken(token);
        if (userId == null) return null;

        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        return createSensorDataInterestAreaDtoForMultipleSensors(sensors, null, null);
    }

    @Override
    public SensorDataInterestAreaDto getTopPublicSensorData() {
        // 1. Registra il tempo di inizio
        long startTime = System.currentTimeMillis();

        // Recupero la lista dei sensori pubblici
        List<Sensor> sensors = sensorRepository.findAllByIsPublic(true);
        int totalPublicSensors = (sensors != null) ? sensors.size() : 0;

        // 2. Elaborazione (passando null come range temporale per avere i record "Top")
        SensorDataInterestAreaDto result = createSensorDataInterestAreaDtoForMultipleSensors(sensors, null, null);

        // 3. Calcolo statistiche finali
        long duration = System.currentTimeMillis() - startTime;
        int dataFoundCount = (result != null && result.getSensorData() != null) ? result.getSensorData().size() : 0;

        // 4. Stampa del Report dettagliato
        System.out.println("------------------------------------------");
        System.out.println("REPORT ESECUZIONE (getTopPublicSensorData):");
        System.out.println("- Tempo impiegato: " + duration + " ms");
        System.out.println("- Record sensori pubblici analizzati: " + totalPublicSensors);
        System.out.println("- Record SensorData (Top) ricavati: " + dataFoundCount);
        System.out.println("");
        // Specifichiamo che il range è null (prendendo l'ultimo dato disponibile in assoluto)
        System.out.println("- [QUERY RANGE START]: NULL (No timeframe limit)");
        System.out.println("- [QUERY RANGE END]  : NULL (No timeframe limit)");
        System.out.println("- [STRATEGIA]         : Recupero dell'ultimo record disponibile");
        System.out.println("------------------------------------------");

        return result;
    }
    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn5Min() {
        return getAllPublicSensorDataInTimeFrame(-5);
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn10Min() {
        return getAllPublicSensorDataInTimeFrame(-10);
    }

    @Override
    public SensorDataInterestAreaDto getAllPublicSensorDataIn15Min() {
        return getAllPublicSensorDataInTimeFrame(-15);
    }

    public SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();

        Optional<SensorData> latestSensorData = sensorDataRepository.findAllBySensorIdAndTimestampBetween(sensorId, fiveMinutesAgo, now)
                .stream()
                .max(Comparator.comparing(SensorData::getTimestamp));

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        latestSensorData.ifPresent(data -> {
            sensorDataList.add(data);
            uniqueKeys.addAll(getSensorKeys(data));
        });

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        return sensorDataInterestAreaDto;
    }



    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId5Min(String interestAreaId, String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();

        Optional<SensorData> latestSensorData = sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId, sensorId, fiveMinutesAgo, now)
                .stream()
                .max(Comparator.comparing(SensorData::getTimestamp));

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        latestSensorData.ifPresent(data -> {
            sensorDataList.add(data);
            uniqueKeys.addAll(getSensorKeys(data));
        });

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        return sensorDataInterestAreaDto;
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId10Min(String interestAreaId, String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        Optional<SensorData> latestSensorData = sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId, sensorId, tenMinutesAgo, now)
                .stream()
                .max(Comparator.comparing(SensorData::getTimestamp));

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        latestSensorData.ifPresent(data -> {
            sensorDataList.add(data);
            uniqueKeys.addAll(getSensorKeys(data));
        });

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        return sensorDataInterestAreaDto;
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId, String token) {
        return getAllSensorDataBySensorIdInTimeFrame(sensorId, token, -5);
    }


    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId10Min(String sensorId, String token) {
        return getAllSensorDataBySensorIdInTimeFrame(sensorId, token, -10);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataBySensorId15Min(String sensorId,  String token) {
        return getAllSensorDataBySensorIdInTimeFrame(sensorId, token, -15);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId5Min(String interestAreaId,  String token) {
        return getAllSensorDataByInterestAreaIdInTimeFrame(interestAreaId, token, -5);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId10Min(String interestAreaId, String token) {
        return getAllSensorDataByInterestAreaIdInTimeFrame(interestAreaId, token, -10);
    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaId15Min(String interestAreaId,  String token) {
        return getAllSensorDataByInterestAreaIdInTimeFrame(interestAreaId, token, -15);
    }

    private SensorDataInterestAreaDto getAllSensorDataBySensorIdInTimeFrame(String sensorId, String token, int minutesAgo) {
        String userId = isValidToken(token);
        assert userId != null;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -minutesAgo);
        Date tenMinutesAgo = calendar.getTime();

        List<Sensor> sensors = sensorRepository.findAllByIdAndUserId(sensorId, userId);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }

        HashSet<String> uniqueKeys = new HashSet<>();

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));

            // Collect unique keys from payloads
            for (SensorData data : sensorDataList) {
                uniqueKeys.addAll(getSensorKeys(data));
            }

        }

        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        System.out.println(sensorDataInterestAreaDto);

        return sensorDataInterestAreaDto;
    }

    private SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdInTimeFrame(String interestAreaId, String token, int minutesAgo) {
        String userId = isValidToken(token);
        assert userId != null;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -minutesAgo);
        Date tenMinutesAgo = calendar.getTime();

        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }

        HashSet<String> uniqueKeys = new HashSet<>();

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));

            // Collect unique keys from payloads
            for (SensorData data : sensorDataList) {
                uniqueKeys.addAll(getSensorKeys(data));
            }

        }

        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        System.out.println(sensorDataInterestAreaDto);

        return sensorDataInterestAreaDto;

    }

    @Override
    public SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId15Min(String interestAreaId, String sensorId) {
        return getAllSensorDataByInterestAreaAndSensorIdInTimeFrame(interestAreaId, sensorId, -15);
    }

    private SensorDataInterestAreaDto getAllSensorDataByInterestAreaAndSensorIdInTimeFrame(String interestAreaId, String sensorId, int minutesAgo) {
        Date fromTime = calculateTimeFromNow(minutesAgo);
        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId, sensorId, fromTime, new Date())
                .stream()
                .max(Comparator.comparing(SensorData::getTimestamp))
                .ifPresent(data -> {
                    sensorDataList.add(data);
                    uniqueKeys.addAll(getSensorKeys(data));
                });

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    public SensorDataInterestAreaDto getAllSensorDataBySensorBetweenDate(DateDto dateDto) {
        String userId = isValidToken(dateDto.getToken());
        assert userId != null;

        Date fromDateUTC = adjustToUTC(dateDto.getForm());
        Date toDateUTC = adjustToUTC(dateDto.getTo());

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        if (dateDto.getSensorId() != null) {
            Sensor sensor = findSensorForUser(dateDto.getSensorId(), dateDto.getInterestAreaId(), userId);
            sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor.getId()), fromDateUTC, toDateUTC)
                    .stream()
                    .max(Comparator.comparing(SensorData::getTimestamp))
                    .ifPresent(data -> {
                        sensorDataList.add(data);
                        uniqueKeys.addAll(getSensorKeys(data));
                    });
        } else {
            List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(dateDto.getInterestAreaId(), userId);
            for (Sensor sensor : sensors) {
                sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor.getId()), fromDateUTC, toDateUTC)
                        .stream()
                        .max(Comparator.comparing(SensorData::getTimestamp))
                        .ifPresent(data -> {
                            sensorDataList.add(data);
                            uniqueKeys.addAll(getSensorKeys(data));
                        });
            }
        }

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    private Date calculateTimeFromNow(int minutesAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, minutesAgo);
        return calendar.getTime();
    }

    private Date adjustToUTC(Date date) {
        return Date.from(date.toInstant().atZone(ZoneId.of("UTC")).minusHours(2).toInstant());
    }

    private Sensor findSensorForUser(String sensorId, String interestAreaId, String userId) {
        Sensor sensor = sensorRepository.findById(sensorId).orElse(null);
        assert sensor != null;
        return sensorRepository.findByIdAndInterestAreaIDAndUserId(String.valueOf(sensor.getId()), interestAreaId, userId);
    }



    @Override
    public SensorDataDto getSensorDataById(Object id) {
        Optional<SensorData> data = sensorDataRepository.findById(id.toString());
        return data.map(sensorDataMapper::sensorDataToSensorDataDto).orElse(null);
    }

    @Override
    public SensorData update(NewSensorDataDto newSensorDataDto) {
        String userId = isValidToken(newSensorDataDto.getToken());
        assert userId != null;
        SensorData existingSensorData = sensorDataRepository.findById(newSensorDataDto.getId()).orElse(null);
        assert existingSensorData != null;
        if (newSensorDataDto.getSensorId() != existingSensorData.getSensorId()) {
            throw new RuntimeException("Invalid credentials");
        }
        Sensor sensor = sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), userId).orElse(null);
        assert sensor != null;
        existingSensorData.setSensorId(newSensorDataDto.getSensorId());
        existingSensorData.setPayloadType(newSensorDataDto.getPayloadType());
        existingSensorData.setLatitude(newSensorDataDto.getLatitude());
        existingSensorData.setLongitude(newSensorDataDto.getLongitude());
        existingSensorData.setTimestamp(Date.from(Instant.now()));


        return sensorDataRepository.save(existingSensorData);
    }

    @Override
    public void delete(String token, String id) {
        String userId = isValidToken(token);
        assert userId != null;
        Optional<SensorData> sensorDataDto = sensorDataRepository.findById(id);
        assert sensorDataDto.isPresent();
        Optional<Sensor> sensor = sensorRepository.findById(sensorDataDto.get().getSensorId());
        assert sensor.isPresent();
        List<Sensor> sensorList = sensorRepository.findAllByIdAndUserId(String.valueOf(sensor.get().getId()),userId);
        if (sensorList != null && !sensorList.isEmpty()) {
            sensorDataRepository.deleteById(id);
        }else {
            throw new RuntimeException("Sensor not found");
        }
    }

    @Override
    public String getProcessedSensorData(String type) {
        //TODO Implementare seriamente
        //List<Sensor> sensors = sensorRepository.findAllByVisibility(true);
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -30);
        Date tenMinutesAgo = calendar.getTime();

        List<SensorData> sensorDataList = sensorDataRepository.findByTimestampBetweenAndPayloadType(tenMinutesAgo, now, "json");
        // Creare il GeoJSON
        String geoJson = createGeoJson(sensorDataList, type);

        return geoJson;
    }


    @Override
    public SensorDataInterestAreaDto getAllSensorDataProcessedByInterestArea(String interestAreaId, String token) {
        String userId = isValidToken(token);
        assert userId != null;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(interestAreaId, userId);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }

        HashSet<String> uniqueKeys = new HashSet<>();

        SensorDataInterestAreaDto sensorDataInterestAreaDto = new SensorDataInterestAreaDto();
        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));

            // Collect unique keys from payloads
            for (SensorData data : sensorDataList) {
                uniqueKeys.addAll(getSensorKeys(data));
            }

        }

        sensorDataInterestAreaDto.setSensorData(sensorDataList);
        sensorDataInterestAreaDto.setSensorAreaTypes(uniqueKeys);

        System.out.println(sensorDataInterestAreaDto);

        return sensorDataInterestAreaDto;





    }

    @Override
    public SensorDataInterestAreaDto getTopPublicSensorDataByInterestAreaId(String interestAreaId) {
        List<Sensor> sensors = sensorRepository.findAllByIsPublicAndInterestAreaID(true, interestAreaId);
        if (sensors == null || sensors.isEmpty()) return null;



        return createSensorDataInterestAreaDtoForMultipleSensors(sensors, null, null);
    }


    private String createGeoJson(List<SensorData> sensorDataList, String type) {
        ObjectMapper mapper = new ObjectMapper();
        // Creare la struttura GeoJSON
        ObjectNode geoJson = mapper.createObjectNode();
        geoJson.put("type", "FeatureCollection");

        ArrayNode features = geoJson.putArray("features");

        for (SensorData data : sensorDataList) {
            ObjectNode feature = mapper.createObjectNode();
            feature.put("type", "Feature");

            ObjectNode geometry = mapper.createObjectNode();
            geometry.put("type", "Point");
            ArrayNode coordinates = geometry.putArray("coordinates");
            coordinates.add(data.getLongitude());
            coordinates.add(data.getLatitude());

            ObjectNode properties = mapper.createObjectNode();

            properties.put("value", getSensorValue(data, type));

            feature.set("geometry", geometry);
            feature.set("properties", properties);

            features.add(feature);
        }

        try {
            return mapper.writeValueAsString(geoJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private double getSensorValue(SensorData data, String type) {
        if (data.getPayload() != null) {
            try {
                // Deserialize the JSON payload into a Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> payload = mapper.readValue(data.getPayload().toString(), Map.class);

                // Find the value based on the provided key (type)
                Object value = payload.getOrDefault(type, 0.0);

                // Check if the retrieved value is a number
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else {
                    return 0.0;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return 0.0;
    }

    private List<String> getSensorKeys(SensorData data) {
        List<String> keys = new ArrayList<>();
        if (data.getPayload() != null) {
            try {
                // Deserialize the JSON payload into a Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> payload = mapper.readValue(data.getPayload().toString(), Map.class);

                // Extract the keys and add them to the list
                keys.addAll(payload.keySet());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return keys;
    }


    private Object parseValue(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }





    private String isValidToken(String token) {
        if (jwtTokenProvider.validateToken(token))
            return jwtTokenProvider.getUserIdFromUserToken(token);
        return null;
    }

    private SensorDataHandler getHandlerForType(String dataType) {
        return switch (dataType.toLowerCase()) {
            case "json" -> new JsonSensorDataHandler();
            case "geojson" -> new GeoJsonSensorDataHandler();
            case "image" -> new ImageSensorDataHandler(new ImageServiceImpl());
            case "shapefile" -> new ShapefileSensorDataHandler(); //Probably not serve
            case "raster" -> new RasterSensorDataHandler(); //TODO
            default -> null;
        };
    }

    private SensorDataInterestAreaDto getAllPublicSensorDataInTimeFrame(int minutesAgo) {
        // 1. Inizio cronometro
        long startTime = System.currentTimeMillis();

        // Recupero sensori
        List<Sensor> sensors = sensorRepository.findAllByIsPublic(true);
        int totalPublicSensors = (sensors != null) ? sensors.size() : 0;

        if (sensors == null || sensors.isEmpty()) {
            System.out.println("Nessun sensore pubblico trovato.");
            return null;
        }

        // 2. Calcolo del Query Range (Corretto per andare nel PASSATO)
        int minutesToSubtract = Math.abs(minutesAgo); // Assicura che sia positivo
        Date toTime = new Date(); // Fine intervallo (Adesso)

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toTime);
        calendar.add(Calendar.MINUTE, -minutesToSubtract); // Sottrae i minuti (es: -15)
        Date fromTime = calendar.getTime(); // Inizio intervallo

        // 3. Elaborazione dati
        SensorDataInterestAreaDto result = createSensorDataInterestAreaDtoForMultipleSensors(sensors, fromTime, toTime);

        // 4. Calcolo statistiche finali
        long duration = System.currentTimeMillis() - startTime;
        int dataFoundCount = (result != null && result.getSensorData() != null) ? result.getSensorData().size() : 0;

        // 5. Stampa del Report dettagliato
        System.out.println("------------------------------------------");
        System.out.println("REPORT ESECUZIONE:");
        System.out.println("- Tempo totale: " + duration + " ms");
        System.out.println("- Sensori processati: " + totalPublicSensors);
        System.out.println("- Record dati ricavati: " + dataFoundCount);
        System.out.println("");
        System.out.println("- [QUERY RANGE START]: " + fromTime);
        System.out.println("- [QUERY RANGE END]  : " + toTime);
        System.out.println("- [RANGE SIZE]       : " + minutesToSubtract + " minuti");
        System.out.println("------------------------------------------");

        return result;
    }


    private SensorDataInterestAreaDto createSensorDataInterestAreaDto(String sensorId, Date fromTime, Date toTime) {
        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        if (fromTime != null && toTime != null) {
            sensorDataRepository.findAllBySensorIdAndTimestampBetween(sensorId, fromTime, toTime)
                    .stream()
                    .max(Comparator.comparing(SensorData::getTimestamp))
                    .ifPresent(data -> {
                        sensorDataList.add(data);
                        uniqueKeys.addAll(getSensorKeys(data));
                    });
            return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
        }else {
            sensorDataRepository.findAllBySensorId(sensorId)
                    .stream()
                    .max(Comparator.comparing(SensorData::getTimestamp))
                    .ifPresent(data -> {
                        sensorDataList.add(data);
                        uniqueKeys.addAll(getSensorKeys(data));
                    });
        }

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    private SensorDataInterestAreaDto createSensorDataInterestAreaDtoForMultipleSensors(List<Sensor> sensors, Date fromTime, Date toTime) {
        if (sensors == null || sensors.isEmpty()) return null;

        List<SensorData> sensorDataList = new ArrayList<>();
        HashSet<String> uniqueKeys = new HashSet<>();

        for (Sensor sensor : sensors) {
            if (fromTime != null && toTime != null) {
                sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor.getId()), fromTime, toTime)
                        .stream()
                        .max(Comparator.comparing(SensorData::getTimestamp))
                        .ifPresent(data -> {
                            sensorDataList.add(data);
                            uniqueKeys.addAll(getSensorKeys(data));
                        });
            }else {
                sensorDataRepository.findAllBySensorId(String.valueOf(sensor.getId()))
                        .stream()
                        .max(Comparator.comparing(SensorData::getTimestamp))
                        .ifPresent(data -> {
                            sensorDataList.add(data);
                            uniqueKeys.addAll(getSensorKeys(data));
                        });
            }
        }

        return buildSensorDataInterestAreaDto(sensorDataList, uniqueKeys);
    }

    private SensorDataInterestAreaDto buildSensorDataInterestAreaDto(List<SensorData> sensorDataList, HashSet<String> uniqueKeys) {
        SensorDataInterestAreaDto dto = new SensorDataInterestAreaDto();
        dto.setSensorData(sensorDataList);
        dto.setSensorAreaTypes(uniqueKeys);
        return dto;
    }


}
