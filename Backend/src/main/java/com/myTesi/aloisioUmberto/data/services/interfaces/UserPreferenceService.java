package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.dto.New.NewUserPreferenceDto;
import com.myTesi.aloisioUmberto.dto.UserPreferenceDto;

import java.util.List;

public interface UserPreferenceService {

    // Metodo per salvare una nuova preferenza nel database
    UserPreferenceDto save(String authHeader, NewUserPreferenceDto newUserPreferenceDTO);

    // Metodo per ottenere tutte le preferenze nel database
    List<UserPreferenceDto> getAllUserPreferences(String authHeader );

    // Metodo per ottenere le preferenze di un utente specifico per ID
    UserPreferenceDto getUserPreferenceByUserId(String authHeader);

    // Metodo per aggiornare le preferenze di un utente esistente nel database
    UserPreferenceDto update(String authHeader,  UserPreferenceDto userPreferenceDTO);

    // Metodo per eliminare le preferenze di un utente dal database
    void delete(String authHeader);

}
