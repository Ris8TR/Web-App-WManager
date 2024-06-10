package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonSensorDataHandler implements SensorDataHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO)  throws IOException {
        Object payload = newSensorDataDTO.getPayload();
        Map<String, Object> newSensorDataMap;

        if (payload instanceof Map) {
            newSensorDataMap = (Map<String, Object>) payload;
        } else {
            newSensorDataMap = objectMapper.convertValue(payload, new TypeReference<>() {
            });
        }

        //TODO vale la pena trasformarlo in stringa?
        String jsonString = objectMapper.writeValueAsString(newSensorDataMap);

        // Save
        data.setPayload(jsonString);
    }

}
