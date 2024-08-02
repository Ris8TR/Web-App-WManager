package com.myTesi.aloisioUmberto.data.entities;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Sensor")
public class Sensor {

        @Id
        private ObjectId id;

        //@Size(min=10)
        @NotNull
        @Field
        private String CompanyName;

        private String sensorId;

        @NotNull
        @Field
        private String password;


        private List<InterestArea> interestAreas; // Aree di interesse dell'utente


}
