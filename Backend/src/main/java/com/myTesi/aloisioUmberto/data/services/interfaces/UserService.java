package com.myTesi.aloisioUmberto.data.services.interfaces;


import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void save(@Valid User user);
    UserDto findByEmail(@Valid String email);
    UserDto saveDto(@Valid NewUserDto newUserDto);
    UserDto update(@Valid String email, @Valid UserDto userDto);
    void deleteByEmail(@Valid String email);

    List<UserDto> getAllUserDtoSortedByLastnameAscending();

}
