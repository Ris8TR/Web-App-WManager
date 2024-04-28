package com.myTesi.aloisioUmberto.dto.New;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class NewUserPreferenceDto {
    private String userId;
    private String[] dataTypesToShow;
    private boolean overlayLayers;
}
