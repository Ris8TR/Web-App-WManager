package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.data.dao.InterestAreaRepository;
import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.data.services.interfaces.InterestAreaService;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class InterestAreaServiceImpl implements InterestAreaService {

    private final InterestAreaRepository interestAreaRepository;

    @Override
    public InterestArea save(NewInterestAreaDto newInterestAreaDto) {
        InterestArea interestArea = new InterestArea();
        interestArea.setUserId(newInterestAreaDto.getUserId());
        interestArea.setName(newInterestAreaDto.getName());
        interestArea.setGeometry(newInterestAreaDto.getGeometry());
        return interestAreaRepository.save(interestArea);
    }

    @Override
    public InterestArea getInterestArea(ObjectId id) {
        return interestAreaRepository.findById(id.toString())
                .orElseThrow(() -> new RuntimeException("Interest Area not found. id: " + id));
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
