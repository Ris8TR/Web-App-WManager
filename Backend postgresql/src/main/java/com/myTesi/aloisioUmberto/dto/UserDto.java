package com.myTesi.aloisioUmberto.dto;


import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@ToString
public class UserDto {

    @Email
    private String email;
    private String firstName;
    private String lastName;
    private String id;

}
