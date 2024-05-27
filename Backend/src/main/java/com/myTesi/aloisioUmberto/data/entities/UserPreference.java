package com.myTesi.aloisioUmberto.data.entities;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "UserPreference")
@Data
public class UserPreference {
    //DA CAMBIARE DI SICURO
    @Id
    private ObjectId id; // Identificatore univoco del documento nel database

    @NotNull
    @Field
    private ObjectId userId; // ID dell'utente a cui appartengono le preferenze di visualizzazione
    private String[] dataTypesToShow; // Tipi di dati geografici da mostrare
    private boolean overlayLayers; // Non mi ricordo nwanche perchè l'ho messo
}
