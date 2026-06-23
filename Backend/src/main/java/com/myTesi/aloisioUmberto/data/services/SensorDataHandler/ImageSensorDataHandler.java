package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.ImageService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ImageSensorDataHandler implements SensorDataHandler {

    private final ImageService imageService;



    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException {
        data.setPayloadType("image");

        // 1. Ottieni la stringa del path/URL dall'imageService
        String imagePath = imageService.processImage(file, newSensorDataDTO.getSensorId(), 1);

        // 2. Crea una mappa per contenere il path
        Map<String, Object> imagePayload = new HashMap<>();
        imagePayload.put("url", imagePath);

        // 3. Imposta la mappa come payload
        data.setPayload(imagePayload);

        System.out.println("Immagine processata e salvata nel payload: " + imagePath);
    }
}



