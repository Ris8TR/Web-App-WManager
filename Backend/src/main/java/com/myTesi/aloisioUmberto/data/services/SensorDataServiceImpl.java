package com.myTesi.aloisioUmberto.data.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorDataMapper;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.*;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
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
    public void SensorDataService(RedisTemplate<String, Object> redisTemplate,
                                  SensorDataRepository sensorDataRepository) {
        this.redisTemplate = redisTemplate;
        this.sensorDataRepository = sensorDataRepository;
    }

    public SensorData saveSensorData(SensorData sensorData) {
        return sensorDataRepository.save(sensorData);
    }

    @Override
    public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
        //TODO aggiungere tutti i controlli
        SensorData data = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDTO);
        String UserId = newSensorDataDTO.getSensorId();
        Optional<Sensor> sensor = sensorRepository.findByUserId(UserId);
        //TODO verificare che la password sia di corretta

        //TODO Rimuovere questo dopo
        if (sensor.isEmpty()) {
            Sensor newSensor = new Sensor();
            newSensor.setCompanyName("TEST");
            newSensor.setUserId(newSensorDataDTO.getUserId());
            sensorRepository.save(newSensor);
            //TODO verificare che il sensore sia di proprietà dell'utente
            data.setSensorId(newSensor.getId().toString());
            //TODO verificare che l'area di interesse sia di proprietà dell'utente
            data.setInterestAreaID(newSensor.getInterestAreaID());
        } else {
            data.setSensorId(sensor.get().getId().toString());
            sensor.get().setInterestAreaID(data.getInterestAreaID());
            sensorRepository.save(sensor.get());
        }

        data.setTimestamp(Date.from(Instant.now()));

        if (file != null && !file.isEmpty()) {
            SensorDataHandler handler = getHandlerForType(newSensorDataDTO.getPayloadType());
            if (handler != null) {
                handler.handle(data, newSensorDataDTO, file);
            }
        } else {
            HashMap<String, Object> payload = new HashMap<>();

            // Rimuovi le parentesi graffe
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

    @Override
    public List<SensorDataDto> getAllSensorData() {
        return sensorDataRepository.findAll().stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    //TODO Serve?!
    public List<SensorDataDto> getAllSensorDataByNow() {
        return sensorDataRepository.findAll().stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBy10Min() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetween(tenMinutesAgo, now).stream()
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

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


    @Override
    public SensorDataDto getSensorDataById(Object id) {
        Optional<SensorData> data = sensorDataRepository.findById(id.toString());
        return data.map(sensorDataMapper::sensorDataToSensorDataDto).orElse(null);
    }

    @Override
    public SensorData update(SensorData newSensorData) {
        SensorData existingSensorData = sensorDataRepository.findById(newSensorData.getSensorId()).orElse(null);
        if (existingSensorData != null) {
            existingSensorData.setSensorId(newSensorData.getSensorId());
            existingSensorData.setPayloadType(newSensorData.getPayloadType());
            existingSensorData.setTimestamp(newSensorData.getTimestamp());
            existingSensorData.setLatitude(newSensorData.getLatitude());
            existingSensorData.setLongitude(newSensorData.getLongitude());

            return sensorDataRepository.save(existingSensorData);
        }
        return null;
    }

    @Override
    public void delete(Object id) {
        sensorDataRepository.deleteById(id.toString());
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
