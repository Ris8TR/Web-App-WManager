package com.myTesi.aloisioUmberto.controller;


import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.services.interfaces.AuthService;
import com.myTesi.aloisioUmberto.dto.LoginDto;
import com.myTesi.aloisioUmberto.dto.TokenDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "Auth") //Name displayed on swagger
public class AuthController {

    private final AuthService authService;


    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/auth/user")
    public ResponseEntity<TokenDto> loginUser(HttpServletRequest req, HttpServletResponse resp, @RequestBody LoginDto loginDto) {
        return authService.loginUser(req, resp, loginDto);
    }

    @PostMapping("/login/resetpsw/{email}")
    public ResponseEntity<Object> resetPass(@PathVariable @Valid String email) {
        return ResponseEntity.ok(authService.resetPass(email));
    }

    @PostMapping("/auth/pass")
    public ResponseEntity<Object> changePass(@RequestBody Map<String, String> requestBody) {
        return ResponseEntity.ok(authService.savePassword(requestBody));
    }

    @PostMapping("/auth/token")
    public ResponseEntity<Boolean> checkToken(@RequestBody String token) {
        return ResponseEntity.ok(authService.checkToken(token));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenDto> refreshToken(@RequestBody String token) {
        return ResponseEntity.ok(authService.refreshToken(token).getBody());
    }










}
