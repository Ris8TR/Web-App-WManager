package com.AloisioUmerto.Tesi.DataHandler.data.service;

import com.AloisioUmerto.Tesi.DataHandler.config.MainServerClient;
import com.AloisioUmerto.Tesi.DataHandler.core.modelMapper.SensorDataMapper;
import com.AloisioUmerto.Tesi.DataHandler.data.dao.SensorDataRepository;
import com.AloisioUmerto.Tesi.DataHandler.data.dao.SensorRepository;
import com.AloisioUmerto.Tesi.DataHandler.data.entities.Sensor;
import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler.*;
import com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler.interfaces.SensorDataHandler;
import com.AloisioUmerto.Tesi.DataHandler.dto.NewSensorDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SensorDataService {
private final SensorDataRepository sensorDataRepository;
private final SensorRepository sensorRepository;
private final SensorDataMapper sensorDataMapper = SensorDataMapper.INSTANCE;
private final MainServerClient mainServerClient;

public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
    SensorData data = sensorDataMapper.newSensorDataDtoToSensorData(newSensorDataDTO);
    String userId = newSensorDataDTO.getUserId();
    Optional<Sensor> sensor = sensorRepository.findByIdAndUserId(newSensorDataDTO.getSensorId(),userId);

    data.setSensorId(sensor.get().getId().toString());
    sensor.get().setInterestAreaID(data.getInterestAreaID());
    sensorRepository.save(sensor.get());


    data.setTimestamp(Date.from(Instant.now()));

    if (file != null && !file.isEmpty()) {
        SensorDataHandler handler = getHandlerForType(newSensorDataDTO.getPayloadType());
        if (handler != null) {
            handler.handle(data, newSensorDataDTO, file);
        }
    } else {
        HashMap<String, Object> payload = new HashMap<>();

        String payloadString = newSensorDataDTO.getPayload().toString().replaceAll("[{}]", "");
        String[] entries = payloadString.split(", ");
        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            String key = keyValue[0].trim();
            Object value = parseValue(keyValue[1].trim());
            payload.put(key, value);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(payload);

        data.setPayload(jsonPayload);
    }

    sensorDataRepository.save(data);

    // Invio dei dati al server principale
    //mainServerClient.sendSensorData(data);

    return data;
}

private Object parseValue(String value) {
    try {
        return Double.parseDouble(value);
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
}