package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class RasterSensorDataHandler implements SensorDataHandler {
    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException {
        data.setPayloadType("raster");
        // Altre operazioni specifiche per raster
    }


}
