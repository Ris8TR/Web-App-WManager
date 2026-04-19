package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public class GeoJsonSensorDataHandler implements SensorDataHandler  {

    @Autowired
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException {
        data.setPayloadType("geojson");
        data.setLatitude(newSensorDataDTO.getLatitude());
        data.setLongitude(newSensorDataDTO.getLongitude());

        // 1. Costruzione dell'oggetto GeoJSON (come stavi facendo)
        Point point = new Point(new LngLatAlt(newSensorDataDTO.getLongitude(), newSensorDataDTO.getLatitude()));
        Feature feature = new Feature();
        feature.setGeometry(point);
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.add(feature);

        // 2. CONVERSIONE DIRETTA IN MAPPA
        // Usiamo convertValue invece di writeValueAsString.
        // Questo trasforma il POJO FeatureCollection in una Map<String, Object>
        Map<String, Object> geoJsonMap = objectMapper.convertValue(featureCollection, new TypeReference<>() {});

        // 3. Salvataggio
        data.setPayload(geoJsonMap);
    }

}
