package com.myTesi.aloisioUmberto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class TokenDto {
    private String token;
    private String refreshToken;
    private String session;
}
