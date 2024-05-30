package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import org.bson.types.ObjectId;

import java.util.List;

public interface InterestAreaService {

    InterestArea save(NewInterestAreaDto newInterestAreaDto);

    InterestArea getInterestArea(ObjectId id);

    List<InterestArea> getInterestAreasByUserId(String userId);

    void deleteInterestArea(ObjectId id);
}