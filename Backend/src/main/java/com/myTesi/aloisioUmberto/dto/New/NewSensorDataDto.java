package com.myTesi.aloisioUmberto.dto.New;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class NewSensorDataDto {
    private String userId;
    private LocalDateTime date;
    private String dataType;
    private double latitude;
    private double longitude;

}
