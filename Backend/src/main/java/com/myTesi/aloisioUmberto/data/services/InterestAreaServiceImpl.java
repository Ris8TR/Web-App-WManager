package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.modelmapper.ModelMapper;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InterestAreaServiceImpl implements InterestAreaService {

    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper = new ModelMapper();
    private final InterestAreaRepository interestAreaRepository;
    private final SensorDataRepository sensorDataRepository;
    private final GeoService geoService;


    //json, immagine, shapefile, raster


    @Override
    public InterestAreaDto save(NewInterestAreaDto newInterestAreaDto) {
        InterestArea interestArea = modelMapper.map(newInterestAreaDto, InterestArea.class);
        interestAreaRepository.save(interestArea);
        System.out.println(interestArea);
        InterestAreaDto interestAreaDto = modelMapper.map(interestArea, InterestAreaDto.class);
        interestAreaDto.setId(interestArea.getId().toString());
        return interestAreaDto;
    }

    @Override
    public InterestArea getInterestArea(ObjectId id) {
        return interestAreaRepository.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Interest Area not found. id: " + id));
    }

    @Override
    public List<InterestAreaDto> getInterestAreasByUserId(String token) {
        String userId = isValidToken(token);
        assert userId != null;
        List<InterestArea> interestAreas = interestAreaRepository.findAllByUserId(userId);

        return interestAreas.stream().map(interestArea -> {
            InterestAreaDto interestAreaDto = modelMapper.map(interestArea, InterestAreaDto.class);
            interestAreaDto.setId(interestArea.getId().toString());
            return interestAreaDto;
        }).collect(Collectors.toList());
    }

    private String isValidToken(String token) {
        if( jwtTokenProvider.validateToken(token))
               return jwtTokenProvider.getUserIdFromUserToken(token);
        return null;
    }


    @Override
    public void deleteInterestArea(ObjectId id) {
        interestAreaRepository.deleteById(id.toString());
    }


    //TODO Verificare che tutto funzioni
    public List<SensorDataDto> getLatestSensorDataInInterestArea(ObjectId interestAreaId) {
        InterestArea interestArea = getInterestArea(interestAreaId);
        List<SensorData> sensors = sensorDataRepository.findAllByDataType(interestArea.getType());

        // Calcolare la data di 10 minuti fa
        Date tenMinutesAgo = Date.from(Instant.now().minusSeconds(600));

        List<SensorDataDto> sensorDataList = new ArrayList<>();
        for (SensorData sensor : sensors) {
            if (geoService.isSensorInInterestArea(sensor.getLatitude(), sensor.getLongitude(), interestArea.getGeometry())) {
                Optional<SensorData> latestSensorData = sensorDataRepository.findTopByDataTypeAndTimestampAfterOrderByTimestampDesc(interestArea.getType(), tenMinutesAgo);
                latestSensorData.ifPresent(sensorData -> sensorDataList.add(modelMapper.map(sensorData, SensorDataDto.class)));
            }
        }
        return sensorDataList;
    }




    //TODO Verificare che tutto funzioni
    public byte[] readShapefileData(String pathToShapefile) throws IOException {
        File file = new File(pathToShapefile);
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        if (!(store instanceof ShapefileDataStore shapefileDataStore)) {
            throw new IOException("Not a valid shapefile");
        }

        shapefileDataStore.setCharset(StandardCharsets.UTF_8);
        SimpleFeatureCollection featureCollection = shapefileDataStore.getFeatureSource().getFeatures();

        List<byte[]> shapefileDataList = new ArrayList<>();

        try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                //TODO Scegliere come eseguire il parsing questo da qui in poi è per test
                String wkt = feature.getDefaultGeometry().toString();
                shapefileDataList.add(wkt.getBytes(StandardCharsets.UTF_8));
            }
        }

        int totalSize = shapefileDataList.stream().mapToInt(arr -> arr.length).sum();
        byte[] shapefileData = new byte[totalSize];
        int currentIndex = 0;

        for (byte[] record : shapefileDataList) {
            System.arraycopy(record, 0, shapefileData, currentIndex, record.length);
            currentIndex += record.length;
        }

        shapefileDataStore.dispose();

        return shapefileData;
    }
}
