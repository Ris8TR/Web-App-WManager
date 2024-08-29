package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
public interface SensorDataRepository extends MongoRepository<SensorData, String> {

    List<SensorData> findAllByPayloadType(String type);

    List<SensorData> findAllBySensorIdAndTimestampBetween(String sensorId, Date from, Date to);

    Optional<SensorData> findTopByPayloadTypeAndTimestampAfterOrderByTimestampDesc(String dataType, Date timestamp);

    List<SensorData> findByTimestampBetween(Date from, Date to);

    List<SensorData> findByTimestampBetweenAndPayloadType(Date from, Date to,String dataType);

    List<SensorData> findAllBySensorId(String sensorId);

    Optional<SensorData> findByIdAndSensorId( String id, String sensorId);

    Optional<SensorData> findTopByTimestampBetweenAndSensorId(Date date, Date date2, @NotNull String sensorId);
}
