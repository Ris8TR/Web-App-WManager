package com.myTesi.aloisioUmberto.data.dao;


import com.myTesi.aloisioUmberto.data.entities.User;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    //Find a SINGLE User using email
    Optional<User> findUserByEmail(String email);

}
