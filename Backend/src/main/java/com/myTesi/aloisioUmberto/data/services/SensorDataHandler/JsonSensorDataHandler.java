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
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException {
        data.setPayloadType("json");
        String fileContent = new String(file.getBytes());

        // Parsing mappa
        Map<String, Object> newSensorDataMap;
        try {
            newSensorDataMap = objectMapper.readValue(fileContent, new TypeReference<Map<String, Object>>() {});
            System.out.println("Payload caricato correttamente: " + newSensorDataMap);
        } catch (IOException e) {
            throw new IllegalArgumentException("Il file non contiene un JSON valido", e);
        }
        //  MongoDB la converte in un oggetto BSON.
        data.setPayload(newSensorDataMap);
    }

}
