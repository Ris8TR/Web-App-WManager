package org.data.dto;




public class InterestAreaDto {
        private String id; // Identificatore univoco del documento nel database
        private String userId; // ID dell'utente a cui appartiene l'area di interesse
        private String name; // Nome dell'area di interesse

        public String getId() {
                return id;
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
                        '}';
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

        private String geometry; // Geometria dell'area
        private String Type; // tipo dell'area
        private String description; // descrizione dell'area

    }


