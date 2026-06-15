package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.core.modelMapper.InterestAreaMapper;
import com.myTesi.aloisioUmberto.core.modelMapper.SensorDataMapper;
import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.interfaces.ImageService;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class InterestAreaServiceImpl implements InterestAreaService {

    private final JwtTokenProvider jwtTokenProvider;
    private final InterestAreaRepository interestAreaRepository;
    private final SensorDataRepository sensorDataRepository;
    private final GeoService geoService;
    private final UserRepository userRepository;
    private final ImageService imageService;

    private final InterestAreaMapper interestAreaMapper = InterestAreaMapper.INSTANCE;
    private final SensorDataMapper sensorDataMapper = SensorDataMapper.INSTANCE;

    /**
     * Helper per validazione token
     */
    private String getValidatedUserId(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromUserToken(token);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    @Override
    @Transactional
    public InterestAreaDto save(NewInterestAreaDto dto, MultipartFile file) throws IOException {
        String userId = getValidatedUserId(dto.getToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        InterestArea area = interestAreaMapper.newInterestAreaDtoToInterestArea(dto);
        area.setUserId(String.valueOf(user.getId()));

        if (file != null && !file.isEmpty()) {
            File tempFile = convertMultipartFileToTempFile(file);
            try {
                area.setGeometry(extractGeometryFromShapefile(tempFile));
            } finally {
                Files.deleteIfExists(tempFile.toPath());
            }
        }

        InterestArea saved = interestAreaRepository.save(area);
        InterestAreaDto resultDto = interestAreaMapper.interestAreaToInterestAreaDto(saved);
        resultDto.setId(saved.getId().toString());
        return resultDto;
    }

    private File convertMultipartFileToTempFile(MultipartFile file) throws IOException {
        String filename = Objects.requireNonNull(file.getOriginalFilename());
        if (!filename.toLowerCase().endsWith(".shp")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be a .shp shapefile");
        }

        File tempFile = Files.createTempFile("upload_", ".shp").toFile();
        file.transferTo(tempFile);
        return tempFile;
    }

    private String extractGeometryFromShapefile(File shapefile) throws IOException {
        FileDataStore store = FileDataStoreFinder.getDataStore(shapefile);
        if (!(store instanceof ShapefileDataStore)) {
            throw new IOException("Invalid shapefile data store");
        }

        ShapefileDataStore ds = (ShapefileDataStore) store;
        try {
            ds.setCharset(StandardCharsets.UTF_8);
            CoordinateReferenceSystem sourceCRS = ds.getSchema().getCoordinateReferenceSystem();
            if (sourceCRS == null) sourceCRS = DefaultGeographicCRS.WGS84;

            CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            SimpleFeatureCollection features = ds.getFeatureSource().getFeatures();
            StringBuilder wktBuilder = new StringBuilder();

            try (SimpleFeatureIterator iterator = features.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    if (geom != null) {
                        Geometry transformed = JTS.transform(geom, transform);
                        wktBuilder.append(transformed.toText()).append(";");
                    }
                }
            }
            return wktBuilder.toString();
        } catch (Exception e) {
            log.error("Error extracting geometry from shapefile", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Geometry transformation failed");
        } finally {
            ds.dispose();
        }
    }

    @Override
    public InterestArea getInterestArea(String id, String token) {
        String userId = getValidatedUserId(token);
        return interestAreaRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interest Area not found"));
    }

    @Override
    public List<InterestAreaDto> getInterestAreasByUserId(String token) {
        String userId = getValidatedUserId(token);
        return interestAreaRepository.findAllByUserId(userId).stream()
                .map(area -> {
                    InterestAreaDto dto = interestAreaMapper.interestAreaToInterestAreaDto(area);
                    dto.setId(area.getId().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InterestAreaDto update(InterestAreaDto dto, MultipartFile geometry, MultipartFile preview) throws IOException {
        String userId = getValidatedUserId(dto.getToken());

        InterestArea area = interestAreaRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interest Area not found"));

        if (!area.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        //area.setType(dto.getType());
        area.setName(dto.getName());
        area.setDescription(dto.getDescription());
        area.setIsPublic(dto.getIsPublic());

        if (geometry != null && !geometry.isEmpty()) {
            File tempFile = convertMultipartFileToTempFile(geometry);
            try {
                area.setGeometry(extractGeometryFromShapefile(tempFile));
            } finally {
                Files.deleteIfExists(tempFile.toPath());
            }
        }

        if (preview != null && !preview.isEmpty()) {
            area.setPreview(imageService.processImage(preview, area.getId().toString(), 2));
        }

        interestAreaRepository.save(area);
        return dto;
    }

    @Override
    @Transactional
    public void deleteInterestArea(ObjectId id, String token) {
        String userId = getValidatedUserId(token);
        InterestArea area = interestAreaRepository.findByIdAndUserId(id.toString(), userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Area not found"));

        interestAreaRepository.deleteById(area.getId().toString());
    }

    @Override
    public List<SensorDataDto> getLatestSensorDataInInterestArea(String interestAreaId, String token) {
        InterestArea area = getInterestArea(interestAreaId, token);
        Date tenMinutesAgo = Date.from(Instant.now().minusSeconds(600));

        // Otteniamo i dati recenti filtrati per tipo una sola volta (Ottimizzazione DB)
        List<SensorData> recentData = sensorDataRepository.findAllByPayloadType(area.getType())
                .stream()
                .filter(d -> d.getTimestamp() != null && d.getTimestamp().after(tenMinutesAgo))
                .collect(Collectors.toList());

        return recentData.stream()
                .filter(data -> geoService.isSensorInInterestArea(data.getLatitude(), data.getLongitude(), area.getGeometry()))
                .map(sensorDataMapper::sensorDataToSensorDataDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterestAreaDto> getAllPublicInterestArea() {
        return interestAreaRepository.findAllByIsPublic(true).stream()
                .map(area -> {
                    InterestAreaDto dto = interestAreaMapper.interestAreaToInterestAreaDto(area);
                    dto.setId(area.getId().toString());
                    return dto;
                })
                .collect(Collectors.toList());
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