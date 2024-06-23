package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.data.services.ImageServiceImpl;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@AllArgsConstructor
public class ImageSensorDataHandler implements SensorDataHandler {

    private final ImageServiceImpl imageService;


    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException {
        data.setPayloadType("image");
        System.out.println("dsdsw");
        data.setPayload(imageService.insertInsertionImage(file,newSensorDataDTO.getSensorId()));
    }



}



