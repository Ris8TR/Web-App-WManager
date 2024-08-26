package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InterestAreaRepository extends MongoRepository<InterestArea, String> {

    List<InterestArea> findAllByUserId(String userId);
    Optional<InterestArea> findByIdAndUserId(String id, String userId);
    List<InterestArea> findAll();


}
