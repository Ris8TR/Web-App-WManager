package com.myTesi.aloisioUmberto.controller;


import com.myTesi.aloisioUmberto.data.services.interfaces.UserService;
import com.myTesi.aloisioUmberto.dto.NewUserDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "User") //Name displayed on swagger

public class UserController {



    private final UserService userService;

    @Autowired (required = false)
    private HttpServletRequest request;


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
    @GetMapping("/all-users")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAllUserDtoSortedByLastnameAscending());
    }

    @GetMapping("/users/{email}")
    public ResponseEntity <UserDto> findByEmail(@PathVariable @Valid String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }


    @PostMapping("/users")
    public ResponseEntity<NewUserDto> addUser(@RequestBody @Valid NewUserDto newUserDto) {
        return ResponseEntity.ok(userService.saveDto(newUserDto));
    }




}
