package com.myTesi.aloisioUmberto.data.services.SensorDataHandler;

import com.myTesi.aloisioUmberto.data.services.SensorDataHandler.interfaces.SensorDataHandler;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDataDto;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.myTesi.aloisioUmberto.data.services.InterestAreaServiceImpl.getBytes;

public class ShapefileSensorDataHandler implements SensorDataHandler {
    @Override
    public void handle(SensorData data, NewSensorDataDto newSensorDataDTO, MultipartFile file) throws IOException {
        data.setPayloadType("shapefile");

        // 1. Leggi i dati dello shapefile (che restituisce byte[])
        byte[] shapefileBinary = readShapefileData((ShapefileDataStore) newSensorDataDTO.getPayload());

        // 2. Avvolgi il byte[] in una Mappa
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("bytes", shapefileBinary); // I byte verranno salvati come BinData nel DB
        payloadMap.put("fileName", file.getOriginalFilename());

        // 3. Imposta la mappa come payload
        data.setPayload(payloadMap);

        // Altre operazioni specifiche per shapefile
    }


    //TODO Verificare che tutto funzioni
    public byte[] readShapefileData(ShapefileDataStore shapefileDataStore) throws IOException {
        return getBytes(shapefileDataStore);
    }

}
