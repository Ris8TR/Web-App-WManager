package com.myTesi.aloisioUmberto.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sensor_data", indexes = {
        @Index(name = "idx_timestamp_payload", columnList = "timestamp, payloadType")
})
@Data
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    private String sensorId;

    @NotNull
    private String payloadType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Temporal(TemporalType.TIMESTAMP)
    private Date savedOnTime;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String payload; // In Postgres salviamo l'oggetto come stringa JSON o testo

    private String interestAreaID;

    @NotNull
    private double latitude;

    @NotNull
    private double longitude;
}