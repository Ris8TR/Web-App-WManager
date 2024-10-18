package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.ColorBarRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorDataRepository;
import com.myTesi.aloisioUmberto.data.dao.SensorRepository;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.Bar.ColorBar;
import com.myTesi.aloisioUmberto.data.entities.Sensor;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.interfaces.ColorBarService;
import com.myTesi.aloisioUmberto.dto.ColorBarDto;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ColorBarServiceImpl implements ColorBarService {

    @Autowired

    private final ColorBarRepository colorBarRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userDao;
    @Autowired
    private ModelMapper modelMapper;

    private String isValidToken(String token) {
        if (jwtTokenProvider.validateToken(token))
            return jwtTokenProvider.getUserIdFromUserToken(token);
        return null;
    }



    @Override
    public Optional<ColorBarDto> findById(String id, String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();

        Optional<ColorBarDto> colorBar = colorBarRepository.findByIdAndUserId(id, userId);
        return colorBar.map(cb -> modelMapper.map(cb, ColorBarDto.class));
    }

    @Override
    public List<ColorBarDto> findBUserId(String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();

        List<ColorBar> colorBars = colorBarRepository.findAllByUserId(userId);

        if (colorBars == null || colorBars.isEmpty()) {
            return Collections.emptyList();
        }

        return colorBars.stream()
                .map(colorBar -> modelMapper.map(colorBar, ColorBarDto.class))
                .collect(Collectors.toList());
    }


    @Override
    public ColorBar save(ColorBarDto colorBarDto) {
        String userId = isValidToken(colorBarDto.getUserId());
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();
        ColorBar  colorBar = modelMapper.map(colorBarDto, ColorBar.class);

        return colorBarRepository.save(colorBar);
    }


    @Override
    public ColorBar update(ColorBar colorBar, String token) {
        String userId = isValidToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        Optional<User> user = userDao.findById(userId);
        assert user.isPresent();

        Optional<ColorBarDto> existingColorBar = colorBarRepository.findByIdAndUserId(String.valueOf(colorBar.getId()), user.get().getId().toString());
        assert existingColorBar.isPresent();
        Optional<ColorBar> ColorBar = colorBarRepository.findById(String.valueOf(colorBar.getId()));
        assert ColorBar.isPresent();

        ColorBar.get().setName(colorBar.getName());
        ColorBar.get().setSensorList(colorBar.getSensorList());
        ColorBar.get().setColorRanges(colorBar.getColorRanges());

        return colorBarRepository.save(ColorBar.get());
    }


}
