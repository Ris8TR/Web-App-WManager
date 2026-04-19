package com.myTesi.aloisioUmberto.data.entities;

import com.myTesi.aloisioUmberto.dto.enumetation.PayloadType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sensors")
public class Sensor {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private String id;

        @NotNull
        private String companyName;

        @Column(name = "user_id", columnDefinition = "TEXT", insertable = true, updatable = true)
        private String userId;

        @Column(columnDefinition = "TEXT")
        private String description;

        @Enumerated(EnumType.STRING)
        private PayloadType payloadType;

        private String type;

        @NotNull
        private String password;

        private String interestAreaID;

        private Boolean isPublic;

        private String colorBarId;

        @ElementCollection
        @CollectionTable(name = "sensor_latitudes", joinColumns = @JoinColumn(name = "sensor_id"))
        private List<Double> latitude;

        @ElementCollection
        @CollectionTable(name = "sensor_longitudes", joinColumns = @JoinColumn(name = "sensor_id"))
        private List<Double> longitude;
}