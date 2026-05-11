package com.myTesi.aloisioUmberto.controller;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserPreferenceService;
import com.myTesi.aloisioUmberto.dto.New.NewUserPreferenceDto;
import com.myTesi.aloisioUmberto.dto.UserPreferenceDto;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/UserPreference")
    public ResponseEntity<UserPreferenceDto> saveUserPreference(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody NewUserPreferenceDto newUserPreferenceDTO) {
        return ResponseEntity.ok(userPreferenceService.save(authHeader, newUserPreferenceDTO));
    }

    @GetMapping("/UserPreference/getAll")
    public ResponseEntity<List<UserPreferenceDto>> getAllUserPreferences(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userPreferenceService.getAllUserPreferences(authHeader));
    }

    @GetMapping("/UserPreference/user")
    public ResponseEntity<UserPreferenceDto> getUserPreferenceByUserId(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userPreferenceService.getUserPreferenceByUserId(authHeader));
    }

    @PutMapping("/UserPreference/user")
    public ResponseEntity<UserPreferenceDto> updateUserPreference(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UserPreferenceDto userPreferenceDTO) {
        return ResponseEntity.ok(userPreferenceService.update(authHeader, userPreferenceDTO));
    }

    @DeleteMapping("/UserPreference/user")
    public ResponseEntity<Void> deleteUserPreference(
            @RequestHeader("Authorization") String authHeader){
        userPreferenceService.delete(authHeader);
        return ResponseEntity.noContent().build();
    }
}