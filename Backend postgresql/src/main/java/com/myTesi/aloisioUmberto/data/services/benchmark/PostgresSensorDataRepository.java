package com.myTesi.aloisioUmberto.data.services.benchmark;

import com.myTesi.aloisioUmberto.data.entities.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface PostgresSensorDataRepository extends JpaRepository<SensorData, String> {


    @Query(value = "SELECT AVG((payload->>'CO2')::numeric) FROM sensor_data " +
            "WHERE sensor_id = :sensorId",
            nativeQuery = true)
    Double findAverageCO2(@Param("sensorId") String sensorId);


    @Query(value = "SELECT AVG((payload->>:key)::numeric) FROM sensor_data " +
            "WHERE sensor_id = :sensorId AND timestamp BETWEEN :from AND :to",
            nativeQuery = true)
    Double findAverageFromJson(@Param("sensorId") String sensorId,
                               @Param("key") String key,
                               @Param("from") Date from,
                               @Param("to") Date to);
}