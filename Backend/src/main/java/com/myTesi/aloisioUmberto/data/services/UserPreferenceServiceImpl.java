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

    private ObjectId validateAndGetUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT Token");
        }

        String userIdFromToken = jwtTokenProvider.getUserIdFromUserToken(token);
        Optional<User> user = userRepository.findById(userIdFromToken);



        return user.get().getId();
    }

    @Override
    public UserPreferenceDto save(String authHeader, NewUserPreferenceDto newUserPreferenceDTO) {
        // 1. Estrai ID dal token (nessun ID passato nell'URL per il save)
        ObjectId userId = validateAndGetUserId(authHeader);

        // 2. Forza l'ID nel DTO per sicurezza
        newUserPreferenceDTO.setUserId(userId);

        UserPreference userPreference = modelMapper.map(newUserPreferenceDTO, UserPreference.class);
        UserPreference saved = userPreferenceRepository.save(userPreference);
        return modelMapper.map(saved, UserPreferenceDto.class);
    }

    @Override
    public List<UserPreferenceDto> getAllUserPreferences(String authHeader) {
        // Valida solo che il token sia valido (operazione da admin o generale)
        validateAndGetUserId(authHeader);
        return userPreferenceRepository.findAll().stream()
                .map(entity -> modelMapper.map(entity, UserPreferenceDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserPreferenceDto getUserPreferenceByUserId(String authHeader) {
        // Verifica che l'utente stia chiedendo i propri dati
        ObjectId userId= validateAndGetUserId(authHeader);

        return userPreferenceRepository.findByUserId(userId.toString())
                .map(preference -> modelMapper.map(preference, UserPreferenceDto.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferences not found"));
    }

    @Override
    public UserPreferenceDto update(String authHeader,  UserPreferenceDto userPreferenceDTO) {
        // 1. Verifica permessi
        ObjectId userId =  validateAndGetUserId(authHeader);

        // 2. Trova esistente
        UserPreference existing = userPreferenceRepository.findByUserId(userId.toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferences not found"));

        // 3. Assicura che l'ID nel DTO sia quello corretto (evita spoofing nel body)
        userPreferenceDTO.setUserId(userId.toString());

        // 4. Mappa i nuovi dati sull'oggetto esistente
        modelMapper.map(userPreferenceDTO, existing);

        UserPreference updated = userPreferenceRepository.save(existing);
        return modelMapper.map(updated, UserPreferenceDto.class);
    }

    @Override
    public void delete(String authHeader) {
        // Verifica permessi
        ObjectId userId= validateAndGetUserId(authHeader);
        userPreferenceRepository.deleteByUserId(userId.toString());
    }
}