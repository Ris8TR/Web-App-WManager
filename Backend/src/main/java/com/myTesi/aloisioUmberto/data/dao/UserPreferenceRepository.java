package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.entities.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserPreferenceRepository  extends MongoRepository<UserPreference, String> {

    List<UserPreference> findByUserId(String userId);
    void deleteByUserId(String userId);

}
