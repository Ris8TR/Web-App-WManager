package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import com.myTesi.aloisioUmberto.data.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SensorDataRepository  extends MongoRepository<SensorData, String> {

    Optional<SensorData> findLatestByUserId(String id);
}
