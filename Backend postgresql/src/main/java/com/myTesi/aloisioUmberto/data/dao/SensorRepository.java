package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.SensorData;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, String> {
    Optional<Sensor> findByUserIdAndIsPublic(String userId, Boolean visibility);
    List<Sensor> findAllByCompanyNameAndIsPublic(@NotNull String companyName, Boolean visibility);
    List<Sensor> findAllByTypeAndIsPublic(String type, Boolean visibility);
    List<Sensor> findAllByIsPublic(Boolean isPublic);
    List<Sensor> findAllByInterestAreaIDAndIsPublicTrue(String interestAreaID);
    List<Sensor> findAllByIsPublicAndInterestAreaID(Boolean visibility, String interestAreaID);
    @Query(value = "SELECT DISTINCT ON (sensor_id) * FROM sensor_data ORDER BY sensor_id, timestamp DESC", nativeQuery = true)
    List<SensorData> findAllLatestData();

    List<Sensor> findAllByCompanyNameAndUserId(@NotNull String companyName, String userId);
    List<Sensor> findAllByUserId(String userId);
    List<Sensor> findAllByUserIdAndType(String userId, String type);
    List<Sensor> findAllByInterestAreaIDAndUserId(String InterestAreaId, String userId);
    Sensor findByIdAndInterestAreaIDAndUserId(String id, String interestAreaID, String userId);
    boolean existsByCompanyNameAndUserIdAndInterestAreaIDAndDescription(String companyName, String userId, String interestAreaID, String description);
    List<Sensor> findAllByIdAndUserId(String id, String userId);

    // Cambiato Object in String per coerenza con JPA
    Optional<Sensor> findByIdAndUserId(String id, String userId);
}