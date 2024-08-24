package org.data.dto;

public class NewSensorDto {

    private String companyName;
    private String password;

    @Override
    public String toString() {
        return "NewSensorDto{" +
                "companyName='" + companyName + '\'' +
                ", password='" + password + '\'' +
                ", SensorId='" + SensorId + '\'' +
                ", description='" + description + '\'' +
                ", userId='" + userId + '\'' +
                ", interestAreaId='" + interestAreaId + '\'' +
                '}';
    }

    public String getInterestAreaId() {
        return interestAreaId;
    }

    public void setInterestAreaId(String interestAreaId) {
        this.interestAreaId = interestAreaId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSensorId() {
        return SensorId;
    }

    public void setSensorId(String sensorId) {
        SensorId = sensorId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    private String SensorId;
    private String description;
    private String userId;
    private String interestAreaId;

}
