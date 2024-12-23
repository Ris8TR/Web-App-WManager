package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@ToString
public class InterestAreaDto {
        private String id; // Identificatore univoco del documento nel database
        private String userId; // ID dell'utente a cui appartiene l'area di interesse
        private String token; // ID dell'utente a cui appartiene l'area di interesse
        private String name; // Nome dell'area di interesse
        private Boolean isPublic; // Nome dell'area di interesse
        private String geometry; // Geometria dell'area
        private String Type; // tipo dell'area
        private String description; // descrizione dell'area

    }


