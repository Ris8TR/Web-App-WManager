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
    SensorDataDto getTopSensorDataBySensorId(String sensorId, String token);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId and interestAreaId
    List<SensorDataDto> getTopSensorDataByInterestAreaIdAndSensorId(String interestAreaId, String sensorId, String token);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by interestAreaId
    List<SensorDataDto> getTopSensorDataByInterestAreaId(String interestAreaId, String token);

    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 5m by sensorId
    List<SensorDataDto> getAllSensorDataBySensorId5Min(String sensorId);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId
    List<SensorDataDto> getAllSensorDataBySensorId10Min(String sensorId);
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by sensorId
    List<SensorDataDto> getAllSensorDataBySensorId15Min(String sensorId);


    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by interestAreaId
    List<SensorDataDto> getAllSensorDataByInterestAreaId5Min(String interestAreaId);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by interestAreaId
    List<SensorDataDto> getAllSensorDataByInterestAreaId10Min(String interestAreaId);
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by interestAreaId
    List<SensorDataDto> getAllSensorDataByInterestAreaId15Min(String interestAreaId);


    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId and interestAreaId
    List<SensorDataDto> getAllSensorDataByInterestAreaIdAndSensorId5Min(String interestAreaId, String sensorId);
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId and interestAreaId
    List<SensorDataDto> getAllSensorDataByInterestAreaIdAndSensorId10Min(String interestAreaId, String sensorId);
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by sensorId and interestAreaId
    List<SensorDataDto> getAllSensorDataByInterestAreaIdAndSensorId15Min(String interestAreaId, String sensorId);


    // Metodo per ottenere i sensorDatadino di un sensore negli ultimi 5m by sensorId
    List<SensorDataDto> getAllPublicSensorDataIn5Min();
    // Metodo per ottenere i sensorDatadto  di un sensore negli ultimi 10m by sensorId
    List<SensorDataDto> getAllPublicSensorDataIn10Min();
    // Metodo per ottenere i sensorDatadto di un sensore negli ultimi 15m by sensorId
    List<SensorDataDto> getAllPublicSensorDataIn15Min();


    // <SensorDataDto> getAllSensorDataBetweenDate(DateDto dateDto);
    List<SensorDataDto> getAllSensorDataBySensorBetweenDate(DateDto dateDto);

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
}