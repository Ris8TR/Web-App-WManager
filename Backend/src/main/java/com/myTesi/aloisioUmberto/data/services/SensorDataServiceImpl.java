package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorDataServiceImpl implements SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Override
    public void saveSensorData(SensorData sensorData) {
        sensorDataRepository.save(sensorData);
    }

    @Override
    public List<SensorData> getAllSensorData() {
        return sensorDataRepository.findAll();
    }

    @Override
    public SensorData getSensorDataById(String id) {
        return sensorDataRepository.findById(id).orElse(null);
    }

    @Override
    public void updateSensorData(String id, SensorData newSensorData) {
        SensorData existingSensorData = sensorDataRepository.findById(id).orElse(null);
        if (existingSensorData != null) {
            // Copia i campi dal nuovo oggetto a quello esistente
            existingSensorData.setUserId(newSensorData.getUserId());
            existingSensorData.setDataType(newSensorData.getDataType());
            existingSensorData.setData(newSensorData.getData());
            existingSensorData.setTimestamp(newSensorData.getTimestamp());
            existingSensorData.setLatitude(newSensorData.getLatitude());
            existingSensorData.setLongitude(newSensorData.getLongitude());

            // Salva l'oggetto aggiornato
            sensorDataRepository.save(existingSensorData);
        }
    }

    @Override
    public void deleteSensorData(String id) {
        sensorDataRepository.deleteById(id);
    }
}

