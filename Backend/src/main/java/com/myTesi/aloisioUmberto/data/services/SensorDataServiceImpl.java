package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.*;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;
    private final UserRepository userDao;
    private final ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public SensorData save(NewSensorDataDto newSensorDataDTO) throws IOException {
        SensorData data = modelMapper.map(newSensorDataDTO, SensorData.class);
        Optional<User> user = userDao.findById(data.getUserId());
        if (user.isPresent()) {
            data.setUserId(newSensorDataDTO.getUserId());
            data.setTimestamp(Date.from(Instant.now()));

            SensorDataHandler handler = getHandlerForType(newSensorDataDTO.getDataType());
            if (handler != null) {
                handler.handle(data, newSensorDataDTO);
                sensorDataRepository.save(data);
                //TODO L'aggiornamento della posizione del sensore dovrebbe andare qui
                return data;
            }
        }
        return null;
    }

    private SensorDataHandler getHandlerForType(String dataType) {
        return switch (dataType.toLowerCase()) {
            case "json" -> new JsonSensorDataHandler();
            case "geojson" -> new GeoJsonSensorDataHandler();
            case "image" -> new ImageSensorDataHandler();
            case "shapefile" -> new ShapefileSensorDataHandler();
            case "raster" -> new RasterSensorDataHandler();
            default -> null;
        };
    }

    @Override
    public List<SensorDataDto> getAllSensorData() {
        return sensorDataRepository.findAll().stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }


    //TODO Serve?!
    public List<SensorDataDto> getAllSensorDataByNow() {
        return sensorDataRepository.findAll().stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBy10Min() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetween(tenMinutesAgo, now).stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }

    @Override
    public SensorDataDto getSensorDataById(Object id) {
        Optional<SensorData> data = sensorDataRepository.findById(id.toString());
        return modelMapper.map(data, SensorDataDto.class);
    }

    @Override
    public SensorData update(SensorData newSensorData) {
        SensorData existingSensorData = sensorDataRepository.findById(newSensorData.getUserId()).orElse(null);
        if (existingSensorData != null) {
            existingSensorData.setUserId(newSensorData.getUserId());
            existingSensorData.setDataType(newSensorData.getDataType());
            existingSensorData.setDate(newSensorData.getDate());
            existingSensorData.setTimestamp(newSensorData.getTimestamp());
            existingSensorData.setLatitude(newSensorData.getLatitude());
            existingSensorData.setLongitude(newSensorData.getLongitude());

            return sensorDataRepository.save(existingSensorData);
        }
        return null;
    }

    @Override
    public void delete(Object id) {
        sensorDataRepository.deleteById(id.toString());
    }
}

