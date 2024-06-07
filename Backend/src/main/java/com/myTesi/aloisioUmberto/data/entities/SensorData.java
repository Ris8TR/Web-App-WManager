package com.myTesi.aloisioUmberto.data.entities;
import com.myTesi.aloisioUmberto.dto.enumetation.Type;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SensorData")
@Data
public class SensorData {
    @Id
    private ObjectId id; // Identificatore univoco del documento nel database

    @NotNull
    @Field
    private String userId; // ID dell'utente che ha inviato i dati

    @NotNull
    @Field
    private String dataType; // Tipo di dati (JSON, GeoJSON, Raster, Shapefile, ecc.)

    private LocalDateTime date; // I dati effettivi inviati dal sensore (può essere rappresentato come una stringa)

    private Date timestamp; // Timestamp dei dati inviati

    //@NotNull
    private String type;

    @NotNull
    @Field
    private double latitude; // Latitudine delle coordinate geografiche

    @NotNull
    @Field
    private double longitude; // Longitudine delle coordinate geografiche
}