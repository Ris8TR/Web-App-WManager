package com.myTesi.aloisioUmberto.controller;


import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserService;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.15.34:4200")
@Tag(name = "User") //Name displayed on swagger

public class UserController {



    private final UserService userService;

    @Autowired (required = false)
    private HttpServletRequest request;
    private final JwtTokenProvider jwtTokenProvider;


    @Operation(
            description = "Get endpoint for user",
            summary = "this is the list of user",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized / Invalid Token",
                            responseCode = "403"
                    )
            }
    )

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/admin/all-users")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAllUserDtoSortedByLastnameAscending());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/admin/{email}")
    public ResponseEntity <UserDto> findByEmail(@PathVariable @Valid String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/admin/{email}")
    public ResponseEntity<UserDto> updateUser(@PathVariable @Valid String email,@RequestBody @Valid UserDto userDto) {
        return ResponseEntity.ok(userService.update(email, userDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/admin/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Valid String email) {
        userService.deleteByEmail(email);
        return ResponseEntity.noContent().build(); // Restituisce 204 No Content
    }

    @PostMapping("/user/newUser")
    public ResponseEntity<UserDto> addUser(@RequestBody @Valid NewUserDto newUserDto) {
        return ResponseEntity.ok(userService.saveDto(newUserDto));
    }

    @GetMapping("/user/private")
    public ResponseEntity<UserDto> getUser(HttpServletRequest request) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(userService.getUser(token));
    }

    @PutMapping("/user/private")
    public ResponseEntity<UserDto> updatePrivateUser(HttpServletRequest request, @RequestBody @Valid UserDto userDto) {
        String token = jwtTokenProvider.getTokenFromRequest(request);
        return ResponseEntity.ok(userService.updatePrivate(token,userDto));
    }




}
