package org.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class InterestAreaDto {
        private String id;
        private String userId;
        private String name;
        private String token;
        private Boolean isPublic;
        private String geometry;
        private String Type;
        private String description;

}
