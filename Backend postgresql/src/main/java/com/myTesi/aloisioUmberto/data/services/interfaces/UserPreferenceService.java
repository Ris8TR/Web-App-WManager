package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.dto.New.NewUserPreferenceDto;
import com.myTesi.aloisioUmberto.dto.UserPreferenceDto;

import java.util.List;

public interface UserPreferenceService {

    // Metodo per salvare una nuova preferenza nel database
    UserPreferenceDto save(NewUserPreferenceDto newUserPreferenceDTO);

    // Metodo per ottenere tutte le preferenze nel database
    List<UserPreferenceDto> getAllUserPreferences();

    // Metodo per ottenere le preferenze di un utente specifico per ID
    UserPreferenceDto getUserPreferenceByUserId(String userId);

    // Metodo per aggiornare le preferenze di un utente esistente nel database
    UserPreferenceDto update(UserPreferenceDto userPreferenceDTO);

    // Metodo per eliminare le preferenze di un utente dal database
    void delete(String userId);

}
