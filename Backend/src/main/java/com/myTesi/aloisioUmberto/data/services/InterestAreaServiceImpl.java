package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
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
    public List<InterestArea> getInterestAreasByUserId(ObjectId userId) {
        return interestAreaRepository.findAllByUserId(userId.toString());
    }



    @Override
    public void deleteInterestArea(ObjectId id) {
        interestAreaRepository.deleteById(id.toString());
    }


/*
    public byte[] readShapefileData(String pathToShapefile) throws IOException {
        FileInputStream shapefileStream = new FileInputStream(pathToShapefile);
        ShapefileReader shapefileReader = new ShapefileReader(shapefileStream);

        List<byte[]> shapefileDataList = new ArrayList<>();

        while (shapefileReader.hasNext()) {
            byte[] record = shapefileReader.nextRecord();
            shapefileDataList.add(record);
        }

        int totalSize = shapefileDataList.stream().mapToInt(arr -> arr.length).sum();
        byte[] shapefileData = new byte[totalSize];
        int currentIndex = 0;

        for (byte[] record : shapefileDataList) {
            System.arraycopy(record, 0, shapefileData, currentIndex, record.length);
            currentIndex += record.length;
        }

        return shapefileData;
    }*/
}
