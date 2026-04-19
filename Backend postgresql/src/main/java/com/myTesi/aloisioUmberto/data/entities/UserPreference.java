package com.myTesi.aloisioUmberto.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_preferences")
@Data
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    private String userId;

    @ElementCollection
    @CollectionTable(name = "user_pref_datatypes", joinColumns = @JoinColumn(name = "preference_id"))
    private List<String> dataTypesToShow; // Usiamo List invece di Array per comodità JPA

    private boolean overlayLayers;
}