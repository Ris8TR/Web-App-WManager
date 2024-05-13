package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SensorDataRepository extends MongoRepository<SensorData, String> {

    default Optional<SensorData> findLatestByUserId(String id) {
        List<SensorData> sensorDataList = findByUserIdOrderByTimestampDesc(id);
        if (sensorDataList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(sensorDataList.getFirst());
        }
    }

    List<SensorData> findByUserIdOrderByTimestampDesc(String id);
}
