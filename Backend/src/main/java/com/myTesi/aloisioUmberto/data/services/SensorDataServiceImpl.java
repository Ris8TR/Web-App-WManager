package com.myTesi.aloisioUmberto.data.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorDataMapper;
import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.*;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.DateDto;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    public SensorData saveSensorData(NewSensorDataDto newSensorDataDto) {
        SensorData sensorData = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDto);
        String userId = isValidToken(newSensorDataDto.getToken());
        assert userId != null;
        Optional<User> user = userDao.findById(userId);
        Optional<Sensor> sensor = sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), userId);
        if (BCrypt.checkpw(newSensorDataDto.getSensorPassword(), user.get().getSensorPassword())) {
            try {
                sensorData.setSensorId(sensor.get().getId().toString());
                //TODO Aggiungere verifica area di interesse
                sensor.get().setInterestAreaID(sensorData.getInterestAreaID());
                sensorRepository.save(sensor.get());
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QUALCOSA NON E' ANDATO PER IL VERSO GIUSTO", e);
            }
        }else {
            throw new RuntimeException("Invalid credentials");
        }

        sensorData.setTimestamp(Date.from(Instant.now()));
        return sensorDataRepository.save(sensorData);
    }

    @Override
    public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
        System.out.println(newSensorDataDTO);
        SensorData data = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDTO);
        String userId = isValidToken(newSensorDataDTO.getToken());
        assert userId != null;
        Optional<User> user = userDao.findById(userId);
        Optional<Sensor> sensor = sensorRepository.findByIdAndUserId(newSensorDataDTO.getSensorId(), userId);
        assert  sensor.isPresent();
        if (BCrypt.checkpw(newSensorDataDTO.getSensorPassword(), user.get().getSensorPassword())) {
            try {
                System.out.println(sensor.get().getId().toString());
            data.setSensorId(sensor.get().getId().toString());
            //TODO Aggiungere verifica area di interesse
            sensor.get().setInterestAreaID(data.getInterestAreaID());
            sensorRepository.save(sensor.get());
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QUALCOSA NON E' ANDATO PER IL VERSO GIUSTO", e);
            }
        }else {
            throw new RuntimeException("Invalid credentials");
        }

        data.setTimestamp(Date.from(Instant.now()));

        if (file != null && !file.isEmpty()) {
            SensorDataHandler handler = getHandlerForType(sensor.get().getType());
            if (handler != null) {
                handler.handle(data, newSensorDataDTO, file);
            }
        } else {
            HashMap<String, Object> payload = new HashMap<>();

            // Rimuove le parentesi graffe
            String payloadString = newSensorDataDTO.getPayload().toString().replaceAll("[{}]", "");

            //parsing del json
            String[] entries = payloadString.split(", ");
            for (String entry : entries) {
                String[] keyValue = entry.split("=");
                String key = keyValue[0].trim();
                Object value = parseValue(keyValue[1].trim()); // Metodo per interpretare il valore come numero o stringa
                payload.put(key, value);
            }

            // Conversione finale
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(payload);

            data.setPayload(jsonPayload);
        }

        sensorDataRepository.save(data);
        // TODO: Aggiornamento della posizione del sensore dovrebbe andare qui
        System.out.println(data);
        return data;
    }


    // Metodo di utilità per interpretare i valori come numeri o stringhe
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



    //TODO Serve?!
    public List<SensorDataDto> getAllSensorDataByNow() {
        return sensorDataRepository.findAll().stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataIn5Min() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetween(tenMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorDataDto> getAllSensorDataIn10Min() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetween(tenMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());    }

    @Override
    public List<SensorDataDto> getAllSensorDataIn15Min() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -15);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetween(tenMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());    }

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBySensorId5Min(String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllBySensorIdAndTimestampBetween(sensorId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    public List<SensorDataDto> getAllSensorDataBySensorId10Min(String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllBySensorIdAndTimestampBetween(sensorId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    public List<SensorDataDto> getAllSensorDataBySensorId15Min(String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -15);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllBySensorIdAndTimestampBetween(sensorId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    /*
    public List<SensorDataDto> getAllSensorDataBy10MinByType(String type) {
        String cacheKey = "SensorData:" + type;
        List<SensorDataDto> sensorDataList = (List<SensorDataDto>) redisTemplate.opsForValue().get(cacheKey);

        if (sensorDataList == null) {
            Date now = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);
            calendar.add(Calendar.MINUTE, -10);
            Date tenMinutesAgo = calendar.getTime();

            sensorDataList = sensorDataRepository.findByTimestampBetweenAndPayloadType(tenMinutesAgo, now, type).stream()
                    .map(sensorDataMapper::sensorDataToSensorDataDto)
                    .collect(Collectors.toList());

            redisTemplate.opsForValue().set(cacheKey, sensorDataList, 5, TimeUnit.MINUTES); // Cache for 5 minutes
        }

        return sensorDataList;
    }

     */

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBySensorBetweenDate(DateDto dateDto) {
        // Supponiamo che le date siano nel fuso orario locale e le convertiamo in UTC
        ZonedDateTime fromDateTime = dateDto.getForm().toInstant().atZone(ZoneId.of("UTC")).minusHours(2);
        ZonedDateTime toDateTime = dateDto.getTo().toInstant().atZone(ZoneId.of("UTC")).minusHours(2);

        Date fromDateUTC = Date.from(fromDateTime.toInstant());
        Date toDateUTC = Date.from(toDateTime.toInstant());

        System.out.println("From Date UTC (after -2 hours): " + fromDateUTC);
        System.out.println("To Date UTC (after -2 hours): " + toDateUTC);

        List<SensorDataDto> results = sensorDataRepository.findAllBySensorIdAndTimestampBetween(dateDto.getSensorId(), fromDateUTC, toDateUTC).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());

        return results;
    }

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBetweenDate(DateDto dateDto) {
        // Supponiamo che le date siano nel fuso orario locale e le convertiamo in UTC
        ZonedDateTime fromDateTime = dateDto.getForm().toInstant().atZone(ZoneId.of("UTC")).minusHours(2);
        ZonedDateTime toDateTime = dateDto.getTo().toInstant().atZone(ZoneId.of("UTC")).minusHours(2);

        Date fromDateUTC = Date.from(fromDateTime.toInstant());
        Date toDateUTC = Date.from(toDateTime.toInstant());

        System.out.println("From Date UTC (after -2 hours): " + fromDateUTC);
        System.out.println("To Date UTC (after -2 hours): " + toDateUTC);

        // Effettua la query utilizzando l'intervallo di date
        List<SensorDataDto> results = sensorDataRepository.findAllByTimestampBetween(fromDateUTC, toDateUTC).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());

        return results;
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
        List<Sensor> sensorList = sensorRepository.findAllByIdAndUserId(sensor.get().getId(),userId);
        if (sensorList != null && !sensorList.isEmpty()) {
            sensorDataRepository.deleteById(id);
        }else {
            throw new RuntimeException("Sensor not found");
        }
    }

    @Override
    public String getProcessedSensorData(String type) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        List<SensorData> sensorDataList = sensorDataRepository.findByTimestampBetweenAndPayloadType(tenMinutesAgo, now, "json");
        // Creare il GeoJSON
        String geoJson = createGeoJson(sensorDataList, type);

        return geoJson;
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
                // Deserializza la stringa JSON nel payload in una Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> payload = mapper.readValue(data.getPayload().toString(), Map.class);

                Object value = switch (type.toLowerCase()) {
                    case "co2" -> payload.get("CO2");
                    case "temperatura" -> payload.get("temperature");
                    case "pressione" -> payload.get("ap");
                    case "umidita" -> payload.get("humidity");
                    default -> 0.0;
                };

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
}
