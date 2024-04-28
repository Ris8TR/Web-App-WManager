package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterestAreaServiceImpl implements InterestAreaService {

    private final InterestAreaRepository interestAreaRepository;

    @Autowired
    public InterestAreaServiceImpl(InterestAreaRepository interestAreaRepository) {
        this.interestAreaRepository = interestAreaRepository;
    }

    @Override
    public InterestArea createInterestArea(ObjectId userId, String name, String geometry) {
        InterestArea interestArea = new InterestArea();
        interestArea.setUserId(userId);
        interestArea.setName(name);
        interestArea.setGeometry(geometry);
        return interestAreaRepository.save(interestArea);
    }

    @Override
    public InterestArea getInterestArea(ObjectId id) {
        return interestAreaRepository.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Interest Area not found with id: " + id));
    }

    @Override
    public List<InterestArea> getInterestAreasByUserId(ObjectId userId) {
        return (List<InterestArea>) interestAreaRepository.findByUserId(userId.toString());
    }

    @Override
    public void deleteInterestArea(ObjectId id) {
        interestAreaRepository.deleteById(id.toString());
    }
}
