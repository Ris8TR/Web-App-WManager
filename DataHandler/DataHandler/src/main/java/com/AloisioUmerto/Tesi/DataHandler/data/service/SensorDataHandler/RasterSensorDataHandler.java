package com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler;


import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler.interfaces.SensorDataHandler;
import com.AloisioUmerto.Tesi.DataHandler.dto.NewSensorDataDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class RasterSensorDataHandler implements SensorDataHandler {
    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException {
        data.setPayloadType("raster");
        // Altre operazioni specifiche per raster
    }


}
