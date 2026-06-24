package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import com.myTesi.aloisioUmberto.dto.SensorDataDto;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface InterestAreaService {

    InterestAreaDto save(NewInterestAreaDto newInterestAreaDto,MultipartFile file) throws IOException;

    InterestArea getInterestArea(String id, String token);

    InterestAreaDto getInterestAreaAdmin(String id);

    List<SensorDataDto> getLatestSensorDataInInterestArea(String interestAreaId, String token);
    List<InterestAreaDto> getAllPublicInterestArea();

    List<InterestAreaDto> getInterestAreasByUserId(String userId);
    List<InterestAreaDto> getInterestAreasByUserIdAdmin(String userId);

    InterestAreaDto update(InterestAreaDto InterestAreaDto, MultipartFile geometry, MultipartFile preview ) throws IOException;


    void deleteInterestArea(ObjectId id, String token);
    void deleteInterestAreaAdmin(ObjectId id);

    InterestAreaDto updateAdmin(InterestAreaDto data, MultipartFile geometry, MultipartFile preview) throws IOException;

}