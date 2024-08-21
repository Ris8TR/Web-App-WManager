package com.myTesi.aloisioUmberto.dto.New;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@ToString
public class NewInterestAreaDto {
    private String userId; // ID dell'utente a cui appartiene l'area di interesse
    private String name; // Nome dell'area di interesse
    private String description; // Nome dell'area di interesse
    private String type; // Nome dell'area di interesse
    private Object file;
    private String geometry; // Geometria dell'area
    private String token;
}
