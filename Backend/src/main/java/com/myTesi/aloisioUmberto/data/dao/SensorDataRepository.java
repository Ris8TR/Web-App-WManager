package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface SensorDataRepository extends MongoRepository<SensorData, String> {

    List<SensorData> findAllByPayloadType(String type);

    List<SensorData> findAllBySensorIdAndTimestampBetween(String sensorId, Date from, Date to);
    List<SensorData> findAllByInterestAreaIDAndTimestampBetween(String interestAreaId, Date startTime, Date endTime);
    List<SensorData> findAllByInterestAreaIDAndSensorIdAndTimestampBetween(String interestAreaId,String sensorId, Date startTime, Date endTime);

    List<SensorData> findAllByTimestampBetween( Date from, Date to);
    Optional<SensorData> findTopBySensorIdOrderByTimestampDesc(String sensorId);

    Optional<SensorData> findTopByPayloadTypeAndTimestampAfterOrderByTimestampDesc(String dataType, Date timestamp);
    List<SensorData> findAllByTimestampBetweenAndSensorId(Date timestamp, Date timestamp2, @NotNull String sensorId);

    List<SensorData> findAllByInterestAreaID(String interestAreaId);

    List<SensorData> findByTimestampBetweenAndPayloadType(Date from, Date to,String dataType);

    List<SensorData> findAllBySensorId(String sensorId);
    List<SensorData> findAllByTimestampBetweenAndPayloadTypeAndSensorId (Date timestamp, Date timestamp2, @NotNull String sensorId, ObjectId id);

    Optional<SensorData> findByIdAndSensorId(ObjectId id, @NotNull String sensorId);

    Optional<SensorData> findTopByTimestampBetweenAndSensorId(Date date, Date date2, @NotNull String sensorId);

    Optional<SensorData> findTopBySensorId(String sensorId);
    List<SensorData> findAllByInterestAreaIDAndSensorId(String interestAreaId,String sensorId);
}
