package com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler;


import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import com.AloisioUmerto.Tesi.DataHandler.data.service.SensorDataHandler.interfaces.SensorDataHandler;
import com.AloisioUmerto.Tesi.DataHandler.dto.NewSensorDataDto;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ShapefileSensorDataHandler implements SensorDataHandler {
    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file)  throws IOException {
        data.setPayloadType("shapefile");
        data.setPayload(readShapefileData((ShapefileDataStore) newSensorDataDTO.getPayload()));
        // Altre operazioni specifiche per shapefile
    }


    //TODO Verificare che tutto funzioni
    public byte[] readShapefileData(ShapefileDataStore shapefileDataStore) throws IOException {
        shapefileDataStore.setCharset(StandardCharsets.UTF_8);
        SimpleFeatureCollection featureCollection = shapefileDataStore.getFeatureSource().getFeatures();

        List<byte[]> shapefileDataList = new ArrayList<>();

        try (SimpleFeatureIterator featureIterator = featureCollection.features()) {
            while (featureIterator.hasNext()) {
                SimpleFeature feature = featureIterator.next();
                // TODO Scegliere come eseguire il parsing, questo da qui in poi è per test
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
