package com.myTesi.aloisioUmberto.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class LoginDto {
    private String email;
    private String password;
}