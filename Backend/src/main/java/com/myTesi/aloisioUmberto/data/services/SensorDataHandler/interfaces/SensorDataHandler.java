package com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;

import java.io.IOException;

public interface SensorDataHandler {
    void handle(SensorData data, NewSensorDataDto newSensorDataDTO) throws IOException;
}