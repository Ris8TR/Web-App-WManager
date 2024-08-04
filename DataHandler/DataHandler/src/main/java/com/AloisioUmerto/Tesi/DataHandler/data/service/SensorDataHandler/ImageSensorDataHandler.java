package com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler;


import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import com.AloisioUmerto.Tesi.DataHandler.data.service.ImageServiceImpl;
import com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler.interfaces.SensorDataHandler;
import com.AloisioUmerto.Tesi.DataHandler.dto.NewSensorDataDto;
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



