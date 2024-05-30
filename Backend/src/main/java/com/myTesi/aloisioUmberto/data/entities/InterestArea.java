package com.myTesi.aloisioUmberto.data.entities;
import com.myTesi.aloisioUmberto.dto.enumetation.Type;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "InterestArea")
@Data
public class InterestArea {

    @Id
    private ObjectId id; // Identificatore univoco del documento nel database

    @NotNull
    @Field
    private String userId; // ID dell'utente a cui appartiene l'area di interesse

    @NotNull
    @Field
    private String name; // Nome dell'area di interesse

    @NotNull
    @Field
    private String geometry; // Geometria dell'area

    @Field
    private byte[] shapefileData; // Dati del Shapefile

    private Type type; //Tipo di dato

    private List<ObjectId> SensorList;
}