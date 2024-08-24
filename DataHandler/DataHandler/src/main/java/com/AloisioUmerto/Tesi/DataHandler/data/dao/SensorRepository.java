package com.AloisioUmerto.Tesi.DataHandler.data.dao;

import com.AloisioUmerto.Tesi.DataHandler.data.entities.Sensor;
import com.AloisioUmerto.Tesi.DataHandler.data.entities.SensorData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SensorRepository extends MongoRepository<Sensor, ObjectId> {
   Optional<Sensor> findByIdAndUserId(Object Id, String sensorId);
}