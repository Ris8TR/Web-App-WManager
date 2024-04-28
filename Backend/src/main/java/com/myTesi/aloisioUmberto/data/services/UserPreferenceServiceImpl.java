package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.UserPreferenceRepository;
import com.myTesi.aloisioUmberto.data.entities.UserPreference;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserPreferenceService;
import com.myTesi.aloisioUmberto.dto.New.NewUserPreferenceDto;
import com.myTesi.aloisioUmberto.dto.UserPreferenceDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;
    private final ModelMapper modelMapper = new ModelMapper();
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserPreferenceDto save(NewUserPreferenceDto newUserPreferenceDTO) {
        UserPreference userPreference = modelMapper.map(newUserPreferenceDTO, UserPreference.class);
        userPreferenceRepository.save(userPreference);
        return modelMapper.map(userPreference, UserPreferenceDto.class);
    }

    @Override
    public List<UserPreferenceDto> getAllUserPreferences() {
        return userPreferenceRepository.findAll().stream().map(s -> modelMapper.map(s, UserPreferenceDto.class)).collect(Collectors.toList());
    }

    //TODO Verificare se funziona
    @Override
    public UserPreferenceDto getUserPreferenceByUserId(String userId) {
          Optional<UserPreference> userPreference = Optional.ofNullable((UserPreference) userPreferenceRepository.findByUserId(userId));
        return modelMapper.map(userPreference, UserPreferenceDto.class);
    }

    @Override
    public UserPreferenceDto update(UserPreferenceDto userPreferenceDTO) {
        UserPreference existingUserPreference = (UserPreference) userPreferenceRepository.findByUserId(userPreferenceDTO.getUserId());
        if (existingUserPreference != null) {
            // Copia i campi dalla nuova preferenza a quella esistente
            existingUserPreference.setDataTypesToShow(userPreferenceDTO.getDataTypesToShow());
            existingUserPreference.setOverlayLayers(userPreferenceDTO.isOverlayLayers());

            // Salva la preferenza aggiornata
            userPreferenceRepository.save(existingUserPreference);
        }
        return modelMapper.map(existingUserPreference, UserPreferenceDto.class);
    }

    @Override
    public void delete(String userId) {
        userPreferenceRepository.deleteByUserId(userId);
    }
}