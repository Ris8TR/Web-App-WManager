package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class UserPreferenceDto {
    private String id;
    private String userId;
    private String[] dataTypesToShow;
    private boolean overlayLayers;
}
