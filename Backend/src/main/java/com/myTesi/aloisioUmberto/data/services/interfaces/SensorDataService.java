package com.myTesi.aloisioUmberto.data.services.interfaces;


import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.DateDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataInterestAreaDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SensorDataService  {


    // Metodo per salvare un nuovo dato nel database
    SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException;


    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 5m by sensorId
    SensorDataInterestAreaDto getTopSensorDataBySensorId(String sensorId, String token);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId and interestAreaId
    SensorDataInterestAreaDto getTopSensorDataByInterestAreaIdAndSensorId(String interestAreaId, String sensorId, String token);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by interestAreaId
    SensorDataInterestAreaDto getTopSensorDataByInterestAreaId(String interestAreaId, String token);
    SensorDataInterestAreaDto getTopPublicSensorData();

    SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId);

    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 5m by sensorId
    SensorDataInterestAreaDto getAllSensorDataBySensorId5Min(String sensorId, String token);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId
    SensorDataInterestAreaDto getAllSensorDataBySensorId10Min(String sensorId, String token);
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by sensorId
    SensorDataInterestAreaDto getAllSensorDataBySensorId15Min(String sensorId, String token);


    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by interestAreaId
    SensorDataInterestAreaDto getAllSensorDataByInterestAreaId5Min(String interestAreaId, String token);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by interestAreaId
    SensorDataInterestAreaDto getAllSensorDataByInterestAreaId10Min(String interestAreaId, String token);
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by interestAreaId
    SensorDataInterestAreaDto getAllSensorDataByInterestAreaId15Min(String interestAreaId, String token);


    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId and interestAreaId
    SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId5Min(String interestAreaId, String sensorId);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId and interestAreaId
    SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId10Min(String interestAreaId, String sensorId);
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by sensorId and interestAreaId
    SensorDataInterestAreaDto getAllSensorDataByInterestAreaIdAndSensorId15Min(String interestAreaId, String sensorId);


    // Metodo per ottenere i sensorDatadino di un sensore negli ultimi 5m by sensorId
    SensorDataInterestAreaDto getAllPublicSensorDataIn5Min();
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId
    SensorDataInterestAreaDto getAllPublicSensorDataIn10Min();
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by sensorId
    SensorDataInterestAreaDto getAllPublicSensorDataIn15Min();

    // Nel file SensorDataService.java
    List<SensorData> getRawDataForSensor(String sensorId, int minutesAgo);

    // <SensorDataDto> getAllSensorDataBetweenDate(DateDto dateDto);
    SensorDataInterestAreaDto getAllSensorDataBySensorBetweenDate(DateDto dateDto);

    // Metodo per ottenere un dato specifico per ID
    SensorDataDto getSensorDataById(Object id);
    // Metodo per aggiornare un dato esistente nel database
    SensorData update(NewSensorDataDto newSensorData);
    public String getProcessedSensorData(String type);
    // Metodo per eliminare un dato dal database
    void delete(String token, String id);

    public SensorData saveSensorData(NewSensorDataDto newSensorDataDto);
    SensorDataDto getLatestSensorDataBySensorId(String token, String id);
    SensorDataInterestAreaDto getAllSensorDataProcessedByInterestArea(String interestAreaId, String token);

    SensorDataInterestAreaDto getTopPublicSensorDataByInterestAreaId(String interestAreaId);

    SensorDataInterestAreaDto getAllPrivateSensorDataBySensorBetweenDate(DateDto dateDto, String token);
}