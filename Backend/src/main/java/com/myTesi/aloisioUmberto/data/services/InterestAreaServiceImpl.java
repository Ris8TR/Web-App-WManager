package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class InterestAreaServiceImpl implements InterestAreaService {

    private final InterestAreaRepository interestAreaRepository;

    @Override
    public InterestArea save(NewInterestAreaDto newInterestAreaDto) {
        InterestArea interestArea = new InterestArea();
        interestArea.setUserId(newInterestAreaDto.getUserId());
        interestArea.setName(newInterestAreaDto.getName());
        interestArea.setGeometry(newInterestAreaDto.getGeometry());
        return interestAreaRepository.save(interestArea);
    }

    @Override
    public InterestArea getInterestArea(ObjectId id) {
        return interestAreaRepository.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Interest Area not found. id: " + id));
    }

    @Override
    public List<InterestArea> getInterestAreasByUserId(String userId) {
        return interestAreaRepository.findAllByUserId(userId.toString());
    }



    @Override
    public void deleteInterestArea(ObjectId id) {
        interestAreaRepository.deleteById(id.toString());
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
                //TODO Scegliere come eseguire il parsing
                // Convert feature to byte array or other representation as needed.
                // For simplicity, let's assume you want to store the feature's WKT (Well-Known Text) representation
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
