package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;

import java.io.IOException;

public class JsonSensorDataHandler implements SensorDataHandler {
    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO)  throws IOException {
        data.setDataType("json");
        // TODO
    }

}
