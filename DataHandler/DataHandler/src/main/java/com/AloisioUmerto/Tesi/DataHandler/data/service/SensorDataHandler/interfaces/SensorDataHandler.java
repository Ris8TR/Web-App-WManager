package com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler.interfaces;


import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import com.AloisioUmerto.Tesi.DataHandler.dto.NewSensorDataDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SensorDataHandler {
    void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException;

}