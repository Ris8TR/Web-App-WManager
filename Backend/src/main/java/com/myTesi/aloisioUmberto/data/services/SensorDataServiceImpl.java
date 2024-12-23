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

    public SensorData saveSensorData(NewSensorDataDto newSensorDataDto) {
        SensorData sensorData = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDto);
        String userId = isValidToken(newSensorDataDto.getToken());
        assert userId != null;
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();
        Optional<Sensor> sensor = sensorRepository.findByIdAndUserId(newSensorDataDto.getSensorId(), userId);
        assert sensor.isPresent();
        if (BCrypt.checkpw(newSensorDataDto.getSensorPassword(), user.get().getSensorPassword())) {
            try {
                sensorData.setSensorId(sensor.get().getId().toString());
                sensor.get().setInterestAreaID(sensorData.getInterestAreaID());
                sensorRepository.save(sensor.get());
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "|Error|", e);
            }
        }else {
            throw new RuntimeException("Invalid credentials");
        }
        sensorData.setTimestamp(newSensorDataDto.getTimestamp());
        sensorData.setSavedOnTime(Date.from(Instant.now()));
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
        String userId = isValidToken(newSensorDataDTO.getToken());
        assert userId != null;
        Optional<User> user = userDao.findById(userId);
        Optional<Sensor> sensor = sensorRepository.findByIdAndUserId(newSensorDataDTO.getSensorId(), userId);
        assert  sensor.isPresent();
        if (BCrypt.checkpw(newSensorDataDTO.getSensorPassword(), user.get().getSensorPassword())) {
            try {
            data.setSensorId(sensor.get().getId().toString());
            //TODO Aggiungere verifica area di interesse
            data.setInterestAreaID(sensor.get().getInterestAreaID());
            sensorRepository.save(sensor.get());
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "|Error|", e);
            }
        }else {
            throw new RuntimeException("Invalid credentials");
        }
        data.setTimestamp(newSensorDataDTO.getTimestamp());
        data.setSavedOnTime(Date.from(Instant.now()));

        if (file != null && !file.isEmpty()) {
            SensorDataHandler handler = getHandlerForType(String.valueOf(sensor.get().getType()));
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

    @Override
    public SensorDataDto getTopSensorDataBySensorId(String sensorId) {


        Sensor sensor = sensorRepository.findById(sensorId).orElse(null);
        assert sensor != null;
        Optional<SensorData> sensorData = sensorDataRepository.findTopBySensorId(sensor.getId().toString());
        return sensorData.map(data -> modelMapper.map(data, SensorDataDto.class)).orElse(null);

    }

    @Override
    public List<SensorDataDto> getTopSensorDataByInterestAreaIdAndSensorId(String interestAreaId, String sensorId) {

        return sensorDataRepository.findAllTopByInterestAreaIDAndSensorId(interestAreaId,sensorId).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorDataDto> getTopSensorDataByInterestAreaId(String interestAreaId) {
        System.out.println(jwtAuthConverter.getId());
        List<Sensor> sensors = sensorRepository.findAllByVisibility(true);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }



        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllTopBySensorId(String.valueOf(sensor.getId()));

        }
        return sensorDataList.stream().map(sensorDataMapper::sensorDataToSensorDataDto).collect(Collectors.toList());

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
    public List<SensorDataDto> getAllPublicSensorDataIn5Min() {

        System.out.println(jwtAuthConverter.getId());
        List<Sensor> sensors = sensorRepository.findAllByVisibility(true);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }


        Date now = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date tenMinutesAgo = calendar.getTime();



        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));


        }
;
        return sensorDataList.stream().map(sensorDataMapper::sensorDataToSensorDataDto).collect(Collectors.toList());

    }

    @Override
    public List<SensorDataDto> getAllPublicSensorDataIn10Min() {
        List<Sensor> sensors = sensorRepository.findAllByVisibility(true);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }


        Date now = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();



        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));


        }
        ;
        return sensorDataList.stream().map(sensorDataMapper::sensorDataToSensorDataDto).collect(Collectors.toList());
    }

    @Override
    public List<SensorDataDto> getAllPublicSensorDataIn15Min() {
        List<Sensor> sensors = sensorRepository.findAllByVisibility(true);
        if (sensors == null || sensors.isEmpty()) {
            return null;
        }


        Date now = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -15);
        Date tenMinutesAgo = calendar.getTime();



        List<SensorData> sensorDataList = new ArrayList<>();
        for (Sensor sensor : sensors) {
            sensorDataList = sensorDataRepository.findAllByTimestampBetweenAndSensorId(tenMinutesAgo, now, String.valueOf(sensor.getId()));
        }
        ;
        return sensorDataList.stream().map(sensorDataMapper::sensorDataToSensorDataDto).collect(Collectors.toList());
    }

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

    public List<SensorDataDto> getAllSensorDataByInterestAreaId5Min(String interestAreaId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllByInterestAreaIDAndTimestampBetween(interestAreaId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    public List<SensorDataDto> getAllSensorDataByInterestAreaId10Min(String interestAreaId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllByInterestAreaIDAndTimestampBetween(interestAreaId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    public List<SensorDataDto> getAllSensorDataByInterestAreaId15Min(String interestAreaId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -15);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllByInterestAreaIDAndTimestampBetween(interestAreaId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorDataDto> getAllSensorDataByInterestAreaIdAndSensorId5Min(String interestAreaId, String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -5);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId,sensorId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());    }

    @Override
    public List<SensorDataDto> getAllSensorDataByInterestAreaIdAndSensorId10Min(String interestAreaId,  String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId,sensorId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SensorDataDto> getAllSensorDataByInterestAreaIdAndSensorId15Min(String interestAreaId, String sensorId) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -15);
        Date fiveMinutesAgo = calendar.getTime();

        return sensorDataRepository.findAllByInterestAreaIDAndSensorIdAndTimestampBetween(interestAreaId,sensorId, fiveMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }


    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBySensorBetweenDate(DateDto dateDto) {
        String userId = isValidToken(dateDto.getToken());
        assert userId != null;
        ZonedDateTime fromDateTime = dateDto.getForm().toInstant().atZone(ZoneId.of("UTC")).minusHours(2);
        ZonedDateTime toDateTime = dateDto.getTo().toInstant().atZone(ZoneId.of("UTC")).minusHours(2);

        Date fromDateUTC = Date.from(fromDateTime.toInstant());
        Date toDateUTC = Date.from(toDateTime.toInstant());

        if (dateDto.getSensorId() != null) {
            Sensor sensor = sensorRepository.findById(dateDto.getSensorId()).orElse(null);
            assert sensor != null;
            Sensor sensor1 = sensorRepository.findByIdAndInterestAreaIDAndUserId(sensor.getId(), dateDto.getInterestAreaId(), userId);
            assert sensor1 !=null;
            return sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor1.getId()), fromDateUTC, toDateUTC).stream()
                    .map(sensorDataMapper::sensorDataToSensorDataDto)
                    .toList();
        } else {
            List<Sensor> sensors = sensorRepository.findAllByInterestAreaIDAndUserId(dateDto.getInterestAreaId(), userId);
            List<SensorDataDto> allResults = new ArrayList<>();

            for (Sensor sensor : sensors) {
                List<SensorDataDto> sensorResults = sensorDataRepository.findAllBySensorIdAndTimestampBetween(String.valueOf(sensor.getId()), fromDateUTC, toDateUTC).stream()
                        .map(sensorDataMapper::sensorDataToSensorDataDto)
                        .toList();
                allResults.addAll(sensorResults);
            }

            return allResults;
        }

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


}
