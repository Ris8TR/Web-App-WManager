package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.interfaces.UserService;
import com.myTesi.aloisioUmberto.dto.New.NewUserDto;
import com.myTesi.aloisioUmberto.dto.UserDto;
import com.myTesi.aloisioUmberto.dto.enumetation.Role;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userDao;
    private final ModelMapper modelMapper = new ModelMapper();
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public void save(User user) {
        userDao.save(user);
    }


    @Override
    public UserDto findByEmail(String email) {
        Optional<User> user  = userDao.findUserByEmail(email);
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> getAllUserDtoSortedByLastnameAscending() {
        return userDao.findAll( Sort.by("lastName").ascending()).stream().map(s -> modelMapper.map(s, UserDto.class)).collect(Collectors.toList());
    }

    @Override
    public NewUserDto saveDto(NewUserDto newUserDto) {

        User user = modelMapper.map(newUserDto, User.class);
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10)));
        user.setRole(Role.valueOf("USER"));
        try {
            userDao.save(user);
            return modelMapper.map(user, NewUserDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "L'indirizzo email è già stato utilizzato.", e);
        }

    }

}
