package com.AloisioUmerto.Tesi.DataHandler.data.entities;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;


@CompoundIndex(def = "{'timestamp': 1, 'payloadType': 1}")
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SensorData")
@Data
public class SensorData {
    @Id
    private ObjectId id;

    @NotNull
    @Field
    private String sensorId;

    @NotNull
    @Field
    private String payloadType;

    private Date timestamp;

    private Object payload;

    @NotNull
    @Field
    private double latitude;

    @NotNull
    @Field
    private double longitude;
}
