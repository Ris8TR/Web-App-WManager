package com.myTesi.aloisioUmberto.data.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;
    private SensorRepository sensorRepository;
    private final UserRepository userDao;
    private final ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
        SensorData data = modelMapper.map(newSensorDataDTO, SensorData.class);
        String sensorId = newSensorDataDTO.getSensorId();
        //TODO
        //String sensorId = jwtTokenProvider.getUserIdFromUserToken(newSensorDataDTO.getSensorId());
        //Optional<Sensor> sensor = sensorRepository.findById(sensorId);
        //if (sensor.isPresent()) {
            data.setSensorId(sensorId);
            newSensorDataDTO.setSensorId(sensorId);
            data.setTimestamp(Date.from(Instant.now()));
            SensorDataHandler handler = getHandlerForType(newSensorDataDTO.getDataType());
            if (handler != null) {
                handler.handle(data, newSensorDataDTO,file);
                sensorDataRepository.save(data);
                //TODO L'aggiornamento della posizione del sensore dovrebbe andare qui


                return data;
            }
        //}
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

    @Override
    public List<SensorDataDto> getAllSensorData() {
        return sensorDataRepository.findAll().stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }


    //TODO Serve?!
    public List<SensorDataDto> getAllSensorDataByNow() {
        return sensorDataRepository.findAll().stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBy10Min() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetween(tenMinutesAgo, now).stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBy10MinByType(String type) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetweenAndPayloadType(tenMinutesAgo, now,type).stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }

    @Override
    public SensorDataDto getSensorDataById(Object id) {
        Optional<SensorData> data = sensorDataRepository.findById(id.toString());
        return modelMapper.map(data, SensorDataDto.class);
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

