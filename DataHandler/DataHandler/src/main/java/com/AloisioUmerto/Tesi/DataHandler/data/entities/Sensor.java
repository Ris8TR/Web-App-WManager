package com.AloisioUmerto.Tesi.DataHandler.data.entities;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Sensor")
public class Sensor {
    @Id
    private ObjectId id;

    @NotNull
    @Field
    private String CompanyName;

    private String sensorId;

    @NotNull
    @Field
    private String password;

}