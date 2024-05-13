package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public SensorData save(NewSensorDataDto newSensorDataDTO) {
        SensorData data = modelMapper.map(newSensorDataDTO, SensorData.class);
        data.setUserId(newSensorDataDTO.getUserId());
        data.setTimestamp(Date.from(Instant.now()));

        return sensorDataRepository.save(data);
    }

    @Override
    public List<SensorDataDto> getAllSensorData() {
        return sensorDataRepository.findAll().stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }

    @Override
    public SensorDataDto getSensorDataById(Object id) {
        Optional<SensorData> data = sensorDataRepository.findById(id.toString());
        return modelMapper.map(data, SensorDataDto.class);
    }

    @Override
    public SensorData update(SensorData newSensorData) {
        SensorData existingSensorData = sensorDataRepository.findById(newSensorData.getUserId().toString()).orElse(null);
        if (existingSensorData != null) {
            // Copia i campi dal nuovo oggetto a quello esistente
            existingSensorData.setUserId(newSensorData.getUserId());
            existingSensorData.setDataType(newSensorData.getDataType());
            existingSensorData.setData(newSensorData.getData());
            existingSensorData.setTimestamp(newSensorData.getTimestamp());
            existingSensorData.setLatitude(newSensorData.getLatitude());
            existingSensorData.setLongitude(newSensorData.getLongitude());

            // Salva l'oggetto aggiornato
            return sensorDataRepository.save(existingSensorData);
        }
        return null;
    }

    @Override
    public void delete(Object id) {
        sensorDataRepository.deleteById(id.toString());
    }
}

