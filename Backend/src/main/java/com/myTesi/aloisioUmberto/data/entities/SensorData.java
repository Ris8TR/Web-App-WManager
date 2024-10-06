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

@CompoundIndex(def = "{'timestamp': 1, 'payloadType': 1}")
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SensorData")
@Data
public class SensorData {
    @Id
    private ObjectId id; // Identificatore univoco del documento nel database

    @NotNull
    @Field
    private String sensorId; // ID dell'utente che ha inviato i dati

    @NotNull
    @Field
    private String payloadType; // Tipo di dati (JSON, GeoJSON, Raster, Shapefile, ecc.)

    private Date timestamp; // Timestamp dei dati inviati

    private Date savedOnTime; // Timestamp dei dati registrati

    @NotNull
    private Object payload;

    private String interestAreaID;

    @NotNull
    @Field
    private double latitude; // Latitudine delle coordinate geografiche

    @NotNull
    @Field
    private double longitude; // Longitudine delle coordinate geografiche
}