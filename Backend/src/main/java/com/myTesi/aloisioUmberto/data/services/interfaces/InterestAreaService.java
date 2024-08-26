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

    List<SensorDataDto> getLatestSensorDataInInterestArea(String interestAreaId, String token);

    List<InterestAreaDto> getInterestAreasByUserId(String userId);

    void deleteInterestArea(ObjectId id);
}