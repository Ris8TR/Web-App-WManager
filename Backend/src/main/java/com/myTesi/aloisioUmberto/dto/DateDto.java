package com.myTesi.aloisioUmberto.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString
public class DateDto {
    private Date form;
    private Date to;
    private String sensorId;
}
