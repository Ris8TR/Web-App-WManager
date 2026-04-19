package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.dto.LoginDto;
import com.myTesi.aloisioUmberto.dto.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService {

    ResponseEntity<TokenDto> loginUser(HttpServletRequest req, HttpServletResponse resp, LoginDto loginDto);
    Object resetPass(String email);
    Boolean savePassword(Map<String, String> requestBody);

    Boolean checkToken(String token);

    ResponseEntity<TokenDto> refreshToken(String token);
}
