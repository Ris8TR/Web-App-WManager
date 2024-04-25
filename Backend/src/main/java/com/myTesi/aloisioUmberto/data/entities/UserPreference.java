package com.myTesi.aloisioUmberto.data.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "UserPreference")
@Data
public class UserPreference {
    private ObjectId id; // Identificatore univoco del documento nel database
    private ObjectId userId; // ID dell'utente a cui appartengono le preferenze di visualizzazione
    private String[] dataTypesToShow; // Tipi di dati geografici da mostrare
    private boolean overlayLayers; // Spe
}
