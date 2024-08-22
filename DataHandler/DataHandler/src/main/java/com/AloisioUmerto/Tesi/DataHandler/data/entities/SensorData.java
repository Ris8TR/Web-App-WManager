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
    private ObjectId id; // Identificatore univoco del documento nel database

    @NotNull
    @Field
    private String sensorId; // ID dell'utente che ha inviato i dati

    @NotNull
    @Field
    private String payloadType; // Tipo di dati (JSON, GeoJSON, Raster, Shapefile, ecc.)

    private Date timestamp; // Timestamp dei dati inviati

    //@NotNull
    private Object payload;

    private String interestAreaID;

    @NotNull
    @Field
    private double latitude; // Latitudine delle coordinate geografiche

    @NotNull
    @Field
    private double longitude; // Longitudine delle coordinate geografiche
}