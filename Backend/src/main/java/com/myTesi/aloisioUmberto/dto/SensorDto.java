package com.myTesi.aloisioUmberto.dto;

import com.myTesi.aloisioUmberto.dto.enumetation.PayloadType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class SensorDto {
    private String companyName;
    private String userId;
    private String Timestamp;
    private String id;
    private String password;
    private String token;
    private String colorBarId;
    private PayloadType payloadType;
    private String description;
    private String type;
    private Boolean isPublic;
    private String interestAreaID;
    private List<Double> latitude;
    private List<Double> longitude;

    public void addCoordinatesAtStart(Double newLatitude, Double newLongitude) {
        if (this.latitude == null) {
            this.latitude = new ArrayList<>();
        }
        if (this.longitude == null) {
            this.longitude = new ArrayList<>();
        }

        this.latitude.addFirst(newLatitude);
        this.longitude.addFirst(newLongitude);
    }

}
