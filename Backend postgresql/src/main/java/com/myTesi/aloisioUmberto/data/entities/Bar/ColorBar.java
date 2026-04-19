package com.myTesi.aloisioUmberto.data.entities.Bar;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "color_bars")
@Data
public class ColorBar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // Cambiato da ObjectId a String

    @NotNull
    @Column(name = "user_id", columnDefinition = "TEXT")
    private String userId;

    @NotNull
    private String name;

    @ElementCollection
    @CollectionTable(name = "color_bar_sensors", joinColumns = @JoinColumn(name = "color_bar_id"))
    @Column(name = "sensor_id")
    private List<String> sensorList;

    private String sensorType;

    // Gestione della lista di oggetti ColorRange
    @ElementCollection
    @CollectionTable(name = "color_bar_ranges", joinColumns = @JoinColumn(name = "color_bar_id"))
    private List<ColorRange> colorRanges;


    public void addSensor(String sensorId) {
        if (sensorList == null) {
            sensorList = new ArrayList<>();
        }
        sensorList.add(sensorId);
    }
}