package com.myTesi.aloisioUmberto.data.services.interfaces;

import com.myTesi.aloisioUmberto.data.entities.Bar.ColorBar;
import com.myTesi.aloisioUmberto.dto.ColorBarDto;
import com.myTesi.aloisioUmberto.dto.New.NewSensorDto;
import com.myTesi.aloisioUmberto.dto.SensorAndAreas;
import com.myTesi.aloisioUmberto.dto.SensorDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ColorBarService {

        Optional<ColorBarDto> findById(String id, String token);

        List<ColorBarDto> findBUserId(String token);

        ColorBar save(ColorBarDto colorBarDto);

        ColorBar update(ColorBar colorBar,String token);


}
