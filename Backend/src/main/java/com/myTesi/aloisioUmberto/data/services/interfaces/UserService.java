package com.myTesi.aloisioUmberto.data.services.interfaces;


import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.UserDto;

import java.util.List;

public interface UserService {

    void save(User user);
    UserDto findByEmail(String email);
    List<UserDto> getAllUserDtoSortedByLastnameAscending();
    NewUserDto saveDto(NewUserDto newUserDto);

}
