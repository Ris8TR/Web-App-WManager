package com.myTesi.aloisioUmberto.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColorBarDto {
    private String id;  // `ObjectId` viene mappato come String
    private String userId;
    private String name;
    private List<String> sensorList;
    private List<ColorRangeDto> colorRanges;
}
