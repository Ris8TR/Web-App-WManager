package com.myTesi.aloisioUmberto.dto.New;

import com.myTesi.aloisioUmberto.data.entities.Bar.ColorRange;
import com.myTesi.aloisioUmberto.dto.enumetation.PayloadType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@ToString
public class NewSensorDto{

    private String companyName;
    private String password;
    private String description;
    private String userId;
    private String token;
    private String type;
    private PayloadType payloadType;
    private String interestAreaId;
    List<ColorRange> ranges;


}
