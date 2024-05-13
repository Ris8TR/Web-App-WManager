package com.myTesi.aloisioUmberto.dto.New;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@ToString
public class NewSensorDataDto {
    private String userId;
    private String dataType;
    private double latitude;
    private double longitude;

}
