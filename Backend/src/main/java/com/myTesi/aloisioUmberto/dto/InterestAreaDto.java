package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@ToString
public class InterestAreaDto {
        private String id;
        private String userId;
        private String token;
        private String name;
        private Boolean isPublic;
        private String geometry;
        private String preview;
        private String description;
    }


