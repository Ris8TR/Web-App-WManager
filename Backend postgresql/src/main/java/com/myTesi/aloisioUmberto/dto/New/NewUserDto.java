package com.myTesi.aloisioUmberto.dto.New;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;


@Data
@NoArgsConstructor
@ToString
public class NewUserDto {

    @NonNull
    @Email
    private String email;
    @NonNull
    @NotBlank
    private String firstName;
    @NonNull
    @NotBlank
    private String lastName;
    @NonNull
    @NotBlank
    private String password;
    @NonNull
    @NotBlank
    private String sensorPassword;

}
