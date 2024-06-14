package com.myTesi.aloisioUmberto.data.dao;


import com.myTesi.aloisioUmberto.data.entities.Sensor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorRepository extends MongoRepository<Sensor, String> {
    List<Sensor> findAllByCompanyName(String companyName);
}
