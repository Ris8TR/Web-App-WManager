package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.UserPreferenceRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.entities.UserPreference;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserPreferenceService;
import com.myTesi.aloisioUmberto.dto.New.NewUserPreferenceDto;
import com.myTesi.aloisioUmberto.dto.UserPreferenceDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper = new ModelMapper();
    private final UserRepository userRepository;


    @Override
    public UserPreferenceDto save(String token, NewUserPreferenceDto newUserPreferenceDTO) {
        String userId = jwtTokenProvider.getUserIdFromUserToken(token);
        newUserPreferenceDTO.setUserId(userId);
        UserPreference userPreference = modelMapper.map(newUserPreferenceDTO, UserPreference.class);
        UserPreference saved = userPreferenceRepository.save(userPreference);
        return modelMapper.map(saved, UserPreferenceDto.class);
    }

    @Override
    public List<UserPreferenceDto> getAllUserPreferences(String token) {
        String userId = jwtTokenProvider.getUserIdFromUserToken(token);
        return userPreferenceRepository.findAll().stream()
                .map(entity -> modelMapper.map(entity, UserPreferenceDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserPreferenceDto getUserPreferenceByUserId(String token) {
        String userId = jwtTokenProvider.getUserIdFromUserToken(token);
        return userPreferenceRepository.findByUserId(userId)
                .map(preference -> modelMapper.map(preference, UserPreferenceDto.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferences not found"));
    }

    @Override
    public UserPreferenceDto update(String token,  UserPreferenceDto userPreferenceDTO) {
        String userId = jwtTokenProvider.getUserIdFromUserToken(token);
        UserPreference existing = userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferences not found"));
        userPreferenceDTO.setUserId(userId);
        modelMapper.map(userPreferenceDTO, existing);

        UserPreference updated = userPreferenceRepository.save(existing);
        return modelMapper.map(updated, UserPreferenceDto.class);
    }

    @Override
    public void delete(String token) {
        String userId = jwtTokenProvider.getUserIdFromUserToken(token);
        userPreferenceRepository.deleteByUserId(userId);
    }
}