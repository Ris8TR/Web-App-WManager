package com.myTesi.aloisioUmberto.data.entities;
import com.myTesi.aloisioUmberto.dto.enumetation.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "InterestArea")
@Data
public class InterestArea {

    private ObjectId id; // Identificatore univoco del documento nel database
    private ObjectId userId; // ID dell'utente a cui appartiene l'area di interesse
    private String name; // Nome dell'area di interesse
    private String geometry; // Geometria dell'area
    private byte[] shapefileData; // Dati del Shapefile
    private Type type; //Tipo di dato
}
