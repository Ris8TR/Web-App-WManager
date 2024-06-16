package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public class JsonSensorDataHandler implements SensorDataHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file)  throws IOException {

        data.setPayloadType("json");
        String fileContent = new String(file.getBytes());
        // Parse the file content to check if it's a valid JSON map
        Map<String, Object> newSensorDataMap;
        try {
            newSensorDataMap = objectMapper.readValue(fileContent, new TypeReference<>() {
            });
            System.out.println(newSensorDataMap);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid file content: not a valid JSON map", e);
        }

        // Set the parsed map as the payload
        //newSensorDataDTO.setPayload(newSensorDataMap);


        //TODO vale la pena trasformarlo in stringa?
        String jsonString = objectMapper.writeValueAsString(newSensorDataMap);

        // Save
        data.setPayload(jsonString);


    }

}
