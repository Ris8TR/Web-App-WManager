package com.myTesi.aloisioUmberto.data.services.interfaces;


import com.myTesi.aloisioUmberto.data.entities.SensorData;

import java.util.List;

public interface SensorDataService  {


    // Metodo per salvare un nuovo dato nel database
    void saveSensorData(SensorData sensorData);

    // Metodo per ottenere tutti i dati nel database
    List<SensorData> getAllSensorData();

    // Metodo per ottenere un dato specifico per ID
    SensorData getSensorDataById(String id);

    // Metodo per aggiornare un dato esistente nel database
    void updateSensorData(String id, SensorData newSensorData);

    // Metodo per eliminare un dato dal database
    void deleteSensorData(String id);
}