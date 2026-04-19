package com.myTesi.aloisioUmberto.data.entities;

import jakarta.persistence.*; // Sostituito javax con jakarta (Spring Boot 3+)
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "InterestArea")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestArea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Genera automaticamente un ID stringa (UUID)
    private String id; // Cambiato da ObjectId a String

// In InterestArea.java

    @NotNull
    @Column(name = "user_id", columnDefinition = "TEXT") // Rimosso insertable/updatable = false
    private String userId;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String geometry;

    private String preview;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean isPublic;

    @Lob // Indica un oggetto di grandi dimensioni (Large Object) per i file binari
    private byte[] shapefileData;

    private String type;


    @ElementCollection
    @CollectionTable(name = "interest_area_sensor_names", joinColumns = @JoinColumn(name = "interest_area_id"))
    @Column(name = "sensor_name")
    private List<String> sensorDataName;


    @ElementCollection
    @CollectionTable(name = "interest_area_sensors", joinColumns = @JoinColumn(name = "interest_area_id"))
    @Column(name = "sensor_id")
    private List<String> sensorList; // Cambiato da ObjectId a String
}
