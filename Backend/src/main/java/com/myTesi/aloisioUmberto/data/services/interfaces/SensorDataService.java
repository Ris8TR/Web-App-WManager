package com.myTesi.aloisioUmberto.data.services.interfaces;


import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;

import java.util.List;

public interface SensorDataService  {


    // Metodo per salvare un nuovo dato nel database
    SensorData save(NewSensorDataDto newSensorDataDTO);

    // Metodo per ottenere tutti i dati nel database
    List<SensorDataDto> getAllSensorData();

    // Metodo per ottenere un dato specifico per ID
    SensorDataDto getSensorDataById(Object id);

    // Metodo per aggiornare un dato esistente nel database
    SensorData update(SensorData newSensorData);

    // Metodo per eliminare un dato dal database
    void delete(Object id);
}