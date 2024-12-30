package org.data.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class NewUserDto {

    public String id;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String sensorPassword;

}
