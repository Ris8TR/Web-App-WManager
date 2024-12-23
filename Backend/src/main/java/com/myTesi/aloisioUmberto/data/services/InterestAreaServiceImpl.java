package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.InterestAreaMapper;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorDataMapper;
import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
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
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InterestAreaServiceImpl implements InterestAreaService {

    private final JwtTokenProvider jwtTokenProvider;
    private final InterestAreaRepository interestAreaRepository;
    private final SensorDataRepository sensorDataRepository;
    private final GeoService geoService;
    private final UserRepository userRepository;
    private final InterestAreaMapper interestAreaMapper = InterestAreaMapper.INSTANCE;
    private final SensorDataMapper sensorDataMapper = SensorDataMapper.INSTANCE;

    public InterestAreaDto save(NewInterestAreaDto newInterestAreaDto, MultipartFile file) throws IOException {

        Optional<User> user = userRepository.findById(jwtTokenProvider.getUserIdFromUserToken(newInterestAreaDto.getToken()));
        if (user.isPresent()) {
            InterestArea interestArea = interestAreaMapper.newInterestAreaDtoToInterestArea(newInterestAreaDto);
            interestArea.setUserId(String.valueOf(user.get().getId())); // Set user ID from token
            interestArea.setDescription(newInterestAreaDto.getDescription());
            interestArea.setType(newInterestAreaDto.getType());
            if (file != null) {
                File convertedFile = convertMultipartFileToFile(file);
                interestArea.setGeometry(extractGeometryFromShapefile(convertedFile));
                convertedFile.delete(); // Clean up the temporary file
            }

            interestAreaRepository.save(interestArea);

            InterestAreaDto interestAreaDto = interestAreaMapper.interestAreaToInterestAreaDto(interestArea);
            interestAreaDto.setId(interestArea.getId().toString());
            interestAreaDto.setDescription(interestArea.getDescription());
            interestAreaDto.setType(interestArea.getType());
            return interestAreaDto;
        }

        return null;
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());

        // Check if the file has a .shp extension
        if (!originalFilename.toLowerCase().endsWith(".shp")) {
            throw new IOException("The provided file is not a shapefile (.shp). Filename: " + originalFilename);
        }

        File convFile = new File(originalFilename);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
    private String extractGeometryFromShapefile(File shapefile) throws IOException {
        FileDataStore store = FileDataStoreFinder.getDataStore(shapefile);
        if (store == null) {
            throw new IOException("Unable to find shapefile at the given path.");
        }

        ShapefileDataStore shapefileDataStore = (ShapefileDataStore) store;
        try {
            shapefileDataStore.setCharset(StandardCharsets.UTF_8);
            SimpleFeatureCollection featureCollection = shapefileDataStore.getFeatureSource().getFeatures();

            // Recupera il sistema di coordinate dello shapefile
            CoordinateReferenceSystem sourceCRS = shapefileDataStore.getSchema().getCoordinateReferenceSystem();
            if (sourceCRS == null) {
                // If CRS is undefined in the shapefile, set a default CRS
                sourceCRS = DefaultGeographicCRS.WGS84;
                // Replace EPSG code with the actual CRS code
            }

            CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;

            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            StringBuilder wktBuilder = new StringBuilder();
            try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
                while (featureIterator.hasNext()) {
                    SimpleFeature feature = featureIterator.next();
                    Geometry originalGeometry = (Geometry) feature.getDefaultGeometry();

                    // Applica la trasformazione
                    Geometry targetGeometry = JTS.transform(originalGeometry, transform);

                    String wkt = targetGeometry.toString();
                    wktBuilder.append(wkt).append(";");
                }
            }

            return wktBuilder.toString();
        } catch (FactoryException | TransformException e) {
            throw new RuntimeException("Error during CRS transformation: " + e.getMessage(), e);
        } finally {
            shapefileDataStore.dispose();
        }
    }

    @Override
    public InterestArea getInterestArea(String id, String token) {
        String userId = isValidToken(token);
        assert userId != null;
        return interestAreaRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Interest Area not found. id: " + id));
    }

    @Override
    public List<InterestAreaDto> getInterestAreasByUserId(String token) {
        String userId = isValidToken(token);
        assert userId != null;
        List<InterestArea> interestAreas = interestAreaRepository.findAllByUserId(userId);

        return interestAreas.stream().map(interestArea -> {
            InterestAreaDto interestAreaDto = interestAreaMapper.interestAreaToInterestAreaDto(interestArea);
            interestAreaDto.setId(interestArea.getId().toString());
            return interestAreaDto;
        }).collect(Collectors.toList());
    }

    @Override
    public InterestAreaDto update(InterestAreaDto interestAreaDto) {
        String userId = isValidToken(interestAreaDto.getToken());
        assert userId != null;
        InterestArea interestArea = interestAreaRepository.findById(interestAreaDto.getId()).orElse(null);
        assert interestArea != null;

        if (!Objects.equals(interestArea.getUserId(), userId)) {
            throw new RuntimeException("Invalid credentials");
        }

        interestArea.setType(interestAreaDto.getType());
        interestArea.setName(interestAreaDto.getName());
        interestArea.setDescription(interestAreaDto.getDescription());
        System.out.println(interestArea);
        interestAreaRepository.save(interestArea);

        return interestAreaDto;
    }

    private String isValidToken(String token) {
        if (jwtTokenProvider.validateToken(token))
            return jwtTokenProvider.getUserIdFromUserToken(token);
        return null;
    }

    @Override
    public void deleteInterestArea(ObjectId id) {
        interestAreaRepository.deleteById(id.toString());
    }

    public List<SensorDataDto> getLatestSensorDataInInterestArea(String interestAreaId, String token) {
        InterestArea interestArea = getInterestArea(interestAreaId, token);
        List<SensorData> sensors = sensorDataRepository.findAllByPayloadType(interestArea.getType());

        Date tenMinutesAgo = Date.from(Instant.now().minusSeconds(600));

        List<SensorDataDto> sensorDataList = new ArrayList<>();
        for (SensorData sensor : sensors) {
            if (geoService.isSensorInInterestArea(sensor.getLatitude(), sensor.getLongitude(), interestArea.getGeometry())) {
                Optional<SensorData> latestSensorData = sensorDataRepository.findTopByPayloadTypeAndTimestampAfterOrderByTimestampDesc(interestArea.getType(), tenMinutesAgo);
                latestSensorData.ifPresent(sensorData -> sensorDataList.add(sensorDataMapper.sensorDataToSensorDataDto(sensorData)));
            }
        }
        return sensorDataList;
    }

    public byte[] readShapefileData(ShapefileDataStore shapefileDataStore) throws IOException {
        return getBytes(shapefileDataStore);
    }

    public static byte[] getBytes(ShapefileDataStore shapefileDataStore) throws IOException {
        shapefileDataStore.setCharset(StandardCharsets.UTF_8);
        SimpleFeatureCollection featureCollection = shapefileDataStore.getFeatureSource().getFeatures();

        List<byte[]> shapefileDataList = new ArrayList<>();

        try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
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