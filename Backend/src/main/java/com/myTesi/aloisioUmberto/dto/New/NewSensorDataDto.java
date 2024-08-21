package com.myTesi.aloisioUmberto.dto.New;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class NewSensorDataDto {
    private String sensorId;
    private LocalDateTime timestamp;
    private String payloadType;
    private double latitude;
    private double longitude;
    private Object payload;
    private String userId;
}
