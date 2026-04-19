package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestAreaRepository extends JpaRepository<InterestArea, String> {

    List<InterestArea> findAllByUserId(String userId);
    List<InterestArea> findAllByIsPublic(Boolean isPublic);
    Optional<InterestArea> findByIdAndUserId(String id, String userId);
    List<InterestArea> findAll();
}