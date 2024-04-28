package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import org.bson.types.ObjectId;

import java.util.List;

public interface InterestAreaService {

    InterestArea createInterestArea(ObjectId userId, String name, String geometry);

    InterestArea getInterestArea(ObjectId id);

    List<InterestArea> getInterestAreasByUserId(ObjectId userId);

    void deleteInterestArea(ObjectId id);
}