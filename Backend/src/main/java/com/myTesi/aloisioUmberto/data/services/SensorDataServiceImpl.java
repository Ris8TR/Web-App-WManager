package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.*;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.services.interfaces.SensorDataService;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private SensorRepository sensorRepository;
    private final UserRepository userDao;
    private final ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public SensorData save(MultipartFile file, NewSensorDataDto newSensorDataDTO) throws IOException {
        SensorData data = modelMapper.map(newSensorDataDTO, SensorData.class);
        String sensorId = newSensorDataDTO.getSensorId();
        //String sensorId = jwtTokenProvider.getUserIdFromUserToken(newSensorDataDTO.getSensorId());
        //Optional<Sensor> sensor = sensorRepository.findById(sensorId);
        //if (sensor.isPresent()) {
            data.setSensorId(sensorId);
            newSensorDataDTO.setSensorId(sensorId);
            data.setTimestamp(Date.from(Instant.now()));
            SensorDataHandler handler = getHandlerForType(newSensorDataDTO.getDataType());
            if (handler != null) {
                handler.handle(data, newSensorDataDTO,file);
                sensorDataRepository.save(data);
                //TODO L'aggiornamento della posizione del sensore dovrebbe andare qui
                System.out.println(data);

                return data;
            }
        //}
        return null;
    }

    private SensorDataHandler getHandlerForType(String dataType) {
        return switch (dataType.toLowerCase()) {
            case "json" -> new JsonSensorDataHandler();
            case "geojson" -> new GeoJsonSensorDataHandler();
            case "image" -> new ImageSensorDataHandler(new ImageServiceImpl());
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

    //TODO IN "GROUND STATION" VA SICURAMENTE CARICATO QUESTO INVECE CHE GET-ALL
    public List<SensorDataDto> getAllSensorDataBy10MinByType(String type) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, -10);
        Date tenMinutesAgo = calendar.getTime();

        return sensorDataRepository.findByTimestampBetweenAndPayloadType(tenMinutesAgo, now,type).stream().map(s -> modelMapper.map(s, SensorDataDto.class)).collect(Collectors.toList());
    }

    @Override
    public SensorDataDto getSensorDataById(Object id) {
        Optional<SensorData> data = sensorDataRepository.findById(id.toString());
        return modelMapper.map(data, SensorDataDto.class);
    }

    @Override
    public SensorData update(SensorData newSensorData) {
        SensorData existingSensorData = sensorDataRepository.findById(newSensorData.getSensorId()).orElse(null);
        if (existingSensorData != null) {
            existingSensorData.setSensorId(newSensorData.getSensorId());
            existingSensorData.setPayloadType(newSensorData.getPayloadType());
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

