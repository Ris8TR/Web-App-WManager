package com.myTesi.aloisioUmberto.data.entities.Bar;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ColorBar")
@Data
public class ColorBar {

    @Id
    private ObjectId id;

    @NotNull
    @Field
    private String userId;

    @NotNull
    @Field
    private String name;

    private List<String> sensorList;

    private String sensorType;

    @Field
    private List<ColorRange> colorRanges;


    public void addSensor(String sensorId) {
        if (sensorList == null) {
            sensorList = new ArrayList<>();
        }

        sensorList.add(sensorId);
    }

}
