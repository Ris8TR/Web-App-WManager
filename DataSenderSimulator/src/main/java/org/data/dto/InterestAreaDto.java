package org.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InterestAreaDto {
        private String id;
        private String userId;
        private String name;
        private String token;

        @JsonProperty("isPublic")
        private Boolean isPublic;

        private String geometry;
        private String Type;
        private String description;

        // Getters and Setters

        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getUserId() {
                return userId;
        }

        public void setUserId(String userId) {
                this.userId = userId;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getToken() {
                return token;
        }

        public void setToken(String token) {
                this.token = token;
        }

        public Boolean getIsPublic() {
                return isPublic;
        }

        public void setIsPublic(Boolean isPublic) {
                this.isPublic = isPublic;
        }

        public String getGeometry() {
                return geometry;
        }

        public void setGeometry(String geometry) {
                this.geometry = geometry;
        }

        public String getType() {
                return Type;
        }

        public void setType(String type) {
                Type = type;
        }

        public String getDescription() {
                return description;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        @Override
        public String toString() {
                return "InterestAreaDto{" +
                        "id='" + id + '\'' +
                        ", userId='" + userId + '\'' +
                        ", name='" + name + '\'' +
                        ", geometry='" + geometry + '\'' +
                        ", Type='" + Type + '\'' +
                        ", description='" + description + '\'' +
                        ", token='" + token + '\'' +
                        ", isPublic=" + isPublic +
                        '}';
        }
}
