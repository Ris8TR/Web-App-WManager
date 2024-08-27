package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.Bar.ColorBar;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorBarRepository  extends MongoRepository<ColorBar, String> {

}