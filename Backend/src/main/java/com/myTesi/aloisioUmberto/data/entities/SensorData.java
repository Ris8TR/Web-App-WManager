package com.myTesi.aloisioUmberto.data.entities;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.util.Date;
import java.util.Map;

@CompoundIndex(def = "{'timestamp': 1, 'payloadType': 1}")
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SensorData")
@CompoundIndex(name = "sensor_timestamp_idx", def = "{'sensorId': 1, 'timestamp': -1}")
@Data
public class SensorData {
    @Id
    private ObjectId id;
    @NotNull
    @Field
    private String sensorId;
    @NotNull
    @Field
    private String payloadType; // Tipo di dati (JSON, GeoJSON, Raster, Shapefile, ecc.)
    private Date timestamp;
    private Date savedOnTime;
    @NotNull
    private Map<String, Object> payload;
    private String interestAreaID;
    @NotNull
    @Field
    private double latitude;
    @NotNull
    @Field
    private double longitude;
}