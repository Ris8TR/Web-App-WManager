package com.myTesi.aloisioUmberto.data.entities;



import com.myTesi.aloisioUmberto.dto.enumetation.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private ObjectId id;

    //@Size(min=10)
    @NotNull
    @Field
    private String firstName;

    //@Size(min=10)
    @NotNull
    @Field
    private String lastName;

    @NotNull
    @Indexed(unique = true) // Indica che l'email deve essere unica
    @Field
    private String email;

    @Field
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Field
    private String password;




    //Altro
    private UserPreference userPreference; // Preferenze di visualizzazione dell'utente
    private List<InterestArea> interestAreas; // Aree di interesse dell'utente


}
