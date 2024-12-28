package org.data.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class NewInterestAreaDto {
    @JsonProperty("isPublic")
    private String userId; // ID dell'utente a cui appartiene l'area di interesse
    private String name; // Nome dell'area di interesse

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description; // Nome dell'area di interesse
    private String type; // Nome dell'area di interesse
    private Boolean isPublic; // Nome dell'area di interesse
    private Object file;

    public Boolean getPublic() {return isPublic;}

    public void setPublic(Boolean aPublic) {isPublic = aPublic;}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getFile() {
        return file;
    }

    public void setFile(Object file) {
        this.file = file;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String geometry; // Geometria dell'area
    private String token;
}
