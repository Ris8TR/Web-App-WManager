package com.AloisioUmerto.Tesi.DataHandler.data.dao;



import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
public interface SensorDataRepository extends MongoRepository<SensorData, ObjectId> {
}
