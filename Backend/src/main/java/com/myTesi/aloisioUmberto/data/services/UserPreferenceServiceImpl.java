package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.data.dao.UserPreferenceRepository;
import com.myTesi.aloisioUmberto.data.entities.UserPreference;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPreferenceServiceImpl implements UserPreferenceService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Override
    public void saveUserPreference(UserPreference userPreference) {
        userPreferenceRepository.save(userPreference);
    }

    @Override
    public List<UserPreference> getAllUserPreferences() {
        return userPreferenceRepository.findAll();
    }

    @Override
    public UserPreference getUserPreferenceByUserId(String userId) {
        return (UserPreference) userPreferenceRepository.findByUserId(userId);
    }

    @Override
    public void updateUserPreference(String userId, UserPreference newUserPreference) {
        UserPreference existingUserPreference = (UserPreference) userPreferenceRepository.findByUserId(userId);
        if (existingUserPreference != null) {
            // Copia i campi dalla nuova preferenza a quella esistente
            existingUserPreference.setDataTypesToShow(newUserPreference.getDataTypesToShow());
            existingUserPreference.setOverlayLayers(newUserPreference.isOverlayLayers());

            // Salva la preferenza aggiornata
            userPreferenceRepository.save(existingUserPreference);
        }
    }

    @Override
    public void deleteUserPreference(String userId) {
        userPreferenceRepository.deleteByUserId(userId);
    }
}