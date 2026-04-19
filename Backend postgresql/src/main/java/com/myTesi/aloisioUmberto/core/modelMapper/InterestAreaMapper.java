package com.myTesi.aloisioUmberto.core.modelMapper;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.opengis.filter.identity.ObjectId;

@Mapper(componentModel = "spring") // Aggiunto per permettere l'iniezione automatica in Spring
public interface InterestAreaMapper {
    InterestAreaMapper INSTANCE = Mappers.getMapper(InterestAreaMapper.class);

    @Mapping(target = "id", ignore = true)
    InterestAreaDto interestAreaToInterestAreaDto(InterestArea interestArea);

    @Mapping(target = "id", ignore = true)
    InterestArea newInterestAreaDtoToInterestArea(NewInterestAreaDto newInterestAreaDto);

}