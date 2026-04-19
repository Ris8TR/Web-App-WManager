package com.myTesi.aloisioUmberto.data.entities;

import com.myTesi.aloisioUmberto.dto.enumetation.Role;
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
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    private String password;

    @NotNull
    private String sensorPassword;

    // Relazione 1:1 con le preferenze
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "preference_id", referencedColumnName = "id")
    private UserPreference userPreference;

// In User.java

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
// Aggiunto insertable = false, updatable = false QUI
    private List<InterestArea> interestAreas;
}