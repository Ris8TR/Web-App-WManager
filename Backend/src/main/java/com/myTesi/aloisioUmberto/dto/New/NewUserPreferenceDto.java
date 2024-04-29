package com.myTesi.aloisioUmberto.dto.New;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@ToString
public class NewUserPreferenceDto {
    private ObjectId userId;
    private String[] dataTypesToShow;
    private boolean overlayLayers;
}
