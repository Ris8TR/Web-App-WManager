package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.data.entities.UserPreference;

import java.util.List;

public interface UserPreferenceService {

    // Metodo per salvare una nuova preferenza nel database
    void saveUserPreference(UserPreference userPreference);

    // Metodo per ottenere tutte le preferenze nel database
    List<UserPreference> getAllUserPreferences();

    // Metodo per ottenere le preferenze di un utente specifico per ID
    UserPreference getUserPreferenceByUserId(String userId);

    // Metodo per aggiornare le preferenze di un utente esistente nel database
    void updateUserPreference(String userId, UserPreference newUserPreference);

    // Metodo per eliminare le preferenze di un utente dal database
    void deleteUserPreference(String userId);

}
