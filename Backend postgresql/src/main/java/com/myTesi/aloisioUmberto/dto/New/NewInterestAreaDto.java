package com.myTesi.aloisioUmberto.dto.New;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class NewInterestAreaDto {
    private String userId;
    private String name;
    private String description;
    private String type;
    private Object file;
    private String geometry;
    private String token;
    private Boolean isPublic;
}
