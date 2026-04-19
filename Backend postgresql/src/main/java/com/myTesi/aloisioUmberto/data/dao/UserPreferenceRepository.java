package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {

    List<UserPreference> findByUserId(String userId);
    void deleteByUserId(String userId);
}