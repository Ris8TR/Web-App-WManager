package com.myTesi.aloisioUmberto.core.modelMapper;

import com.myTesi.aloisioUmberto.data.entities.InterestArea;
import com.myTesi.aloisioUmberto.dto.InterestAreaDto;
import com.myTesi.aloisioUmberto.dto.New.NewInterestAreaDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper
public interface InterestAreaMapper {
    InterestAreaMapper INSTANCE = Mappers.getMapper(InterestAreaMapper.class);

    @Mapping(target = "id", ignore = true)
    InterestAreaDto interestAreaToInterestAreaDto(InterestArea interestArea);

    @Mapping(target = "id", ignore = true)
    InterestArea newInterestAreaDtoToInterestArea(NewInterestAreaDto newInterestAreaDto);

    default String map(ObjectId value) {
        return value != null ? value.toString() : null;
    }

    default ObjectId map(String value) {
        return value != null ? new ObjectId(value) : null;
    }
}