package com.myTesi.aloisioUmberto.data.entities;

import com.myTesi.aloisioUmberto.dto.enumetation.PayloadType;
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
        private String companyName;
        private String userId;
        private String description;
        private PayloadType payloadType;
        private String type;
        @NotNull
        @Field
        private String password;
        private String interestAreaID;
        private Boolean visibility;
        private String colorBarId;
}
