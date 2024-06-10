package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Geometry;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class GeoJsonSensorDataHandler implements SensorDataHandler  {

    @Autowired
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO) throws IOException {
        data.setPayloadType("geojson");
        data.setLatitude(newSensorDataDTO.getLatitude());
        data.setLongitude(newSensorDataDTO.getLongitude());


        Point point = new Point(new LngLatAlt(newSensorDataDTO.getLongitude(), newSensorDataDTO.getLatitude()));

        Feature feature = new Feature();
        feature.setGeometry(point);
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.add(feature);

        //TODO vale la pena trasformarlo in stringa?
        String geoJsonString = objectMapper.writeValueAsString(featureCollection);
        data.setPayload(geoJsonString);
    }

}
