package com.myTesi.aloisioUmberto.data.dao;


import com.myTesi.aloisioUmberto.data.entities.Sensor;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends MongoRepository<Sensor, String> {
    List<Sensor> findAllByCompanyNameAndUserId(@NotNull String companyName, String userId);
    List<Sensor> findAllByUserId(String userId);
    List<Sensor> findAllByUserIdAndType(String userId, String type);
    List<Sensor> findAllByInterestAreaIDAndUserId(String InterestAreaId, String userId);
    Sensor findByIdAndInterestAreaIDAndUserId(ObjectId id, String interestAreaID, String userId);
    boolean existsByCompanyNameAndUserIdAndInterestAreaIDAndDescription(String companyName, String userId, String interestAreaID , String description);
    List<Sensor> findAllByIdAndUserId(ObjectId id, String userId);
    Optional<Sensor> findByIdAndUserId(Object Id, String sensorId);
}
