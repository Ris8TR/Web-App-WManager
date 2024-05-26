package com.myTesi.aloisioUmberto.data.entities;
import com.myTesi.aloisioUmberto.dto.enumetation.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "SensorData")
@Data
public class SensorData {
    private ObjectId id; // Identificatore univoco del documento nel database
    private String userId; // ID dell'utente che ha inviato i dati
    private String dataType; // Tipo di dati (JSON, GeoJSON, Raster, Shapefile, ecc.)
    private String data; // I dati effettivi inviati dal sensore (può essere rappresentato come una stringa)
    private Date timestamp; // Timestamp dei dati inviati
    private double latitude; // Latitudine delle coordinate geografiche
    private double longitude; // Longitudine delle coordinate geografiche
}