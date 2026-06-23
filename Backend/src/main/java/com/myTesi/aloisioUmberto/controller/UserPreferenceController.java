package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserPreferenceService;
import com.myTesi.aloisioUmberto.dto.New.NewUserPreferenceDto;
import com.myTesi.aloisioUmberto.dto.UserPreferenceDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "UserPreference")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/UserPreference")
    public ResponseEntity<UserPreferenceDto> saveUserPreference(HttpServletRequest request, @RequestBody NewUserPreferenceDto newUserPreferenceDTO) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(userPreferenceService.save(token, newUserPreferenceDTO));
    }

    @GetMapping("/UserPreference/getAll")
    public ResponseEntity<List<UserPreferenceDto>> getAllUserPreferences(HttpServletRequest request ) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(userPreferenceService.getAllUserPreferences(token));
    }

    @GetMapping("/UserPreference/user")
    public ResponseEntity<UserPreferenceDto> getUserPreferenceByUserId(HttpServletRequest request) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(userPreferenceService.getUserPreferenceByUserId(token));
    }

    @PutMapping("/UserPreference/user")
    public ResponseEntity<UserPreferenceDto> updateUserPreference(HttpServletRequest request ,@RequestBody UserPreferenceDto userPreferenceDTO) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(userPreferenceService.update(token, userPreferenceDTO));
    }

    @DeleteMapping("/UserPreference/user")
    public ResponseEntity<Void> deleteUserPreference(HttpServletRequest request ){
        String token = jwtTokenProvider.getTokenFromRequest(request);
        userPreferenceService.delete(token);
        return ResponseEntity.noContent().build();
    }
}